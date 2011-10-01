(ns foreclojure.users
  (:require [ring.util.response       :as response]
            [sandbar.stateful-session :as session])
  (:use     [foreclojure.utils        :only [from-mongo row-class rank-class get-user with-user]]
            [foreclojure.template     :only [def-page content-page]]
            [foreclojure.config       :only [config repo-url]]
            [somnium.congomongo       :only [fetch-one fetch update!]]
            [compojure.core           :only [defroutes GET POST]]
            [hiccup.form-helpers      :only [form-to hidden-field]]
            [hiccup.page-helpers      :only [link-to]]
            [clojure.contrib.json     :only [json-str]]))

(def golfer-tags (into [:contributor]
                       (when (:golfing-active config)
                         [:golfer])))

(defn get-user-id [name]
  (:_id
   (fetch-one :users
              :where {:user name}
              :only [:_id])))

(defn get-users []
  (from-mongo
   (fetch :users
          :only [:user :solved :contributor])))

(defn get-ranked-users []
  (let [users (get-users)
        tied-groups (map val
                         (sort-by #(-> % key -)
                                  (group-by #(count (or (:solved %) []))
                                            users)))]
    (first
     (reduce (fn [[user-list position rank] new-group]
               [(into user-list
                      (for [user (sort-by :user new-group)]
                        (into user {:rank     rank
                                    :position position})))
                (inc position)
                (+ rank (count new-group))])
             [[] 1 1]
             tied-groups))))

(defn get-top-100-and-current-user [username]
  (let [ranked-users      (get-ranked-users)
        this-user         (first (filter (comp #{username} :user)
                                         ranked-users))
        this-user-ranking (update-in this-user [:rank] #(str (or % "?") " out of " (count ranked-users)))]
    {:user-ranking this-user-ranking
     :top-100 (take 100 ranked-users)}))

(defn golfer? [user]
  (some user golfer-tags))

(defn disable-codebox? [user]
  (true? (:disable-code-box user)))

(defn hide-solutions? [user]
  (true? (:hide-solutions user)))

(defn email-address [username]
  (:email (fetch-one :users :where {:user username})))

(defn mailto [username]
  (link-to (str "mailto:" (email-address username))
           username))

(defn format-user-ranking [{:keys [rank user contributor solved]}]
  (when user
    [:div
    [:h2 "Your Ranking"]
    [:div.ranking (str "Username: ")
     (when contributor [:span.contributor "* "])
     [:a.user-profile-link {:href (str "/user/" user)} user]]
    [:div.ranking (str "Rank: " rank)]
    [:div.ranking (str "Problems Solved: " (count solved))]
    [:br]
    [:br]]))

(defn follow-url [username follow?]
  (str "/user/" (if follow? "follow" "unfollow") "/" username))

(defn following-checkbox [current-user-id following user-id user]
  (when (and current-user-id (not= current-user-id user-id))
    (let [following? (contains? following user-id)]
      (form-to [:post (follow-url user (not following?))]
               [:input.following {:type "checkbox" :checked following?}]
               [:span.following (when following? "yes")]))))

(defn generate-user-list [user-set]
  (let [[user-id following]
        (when (session/session-get :user)
          (with-user [{:keys [_id following]}]
            [_id (set following)]))]
    (list
     [:br]
     [:table#user-table.my-table
      [:thead
       [:tr
        [:th {:style "width: 40px;" } "Rank"]
        [:th {:style "width: 200px;"} "Username"]
        [:th {:style "width: 180px;"} "Problems Solved"]
        [:th "Following"]]]
      (map-indexed (fn [rownum {:keys [_id position rank user contributor solved]}]
                     [:tr (row-class rownum)
                      [:td (rank-class position) rank]
                      [:td
                       (when contributor [:span.contributor "* "])
                       [:a.user-profile-link {:href (str "/user/" user)} user]]
                      [:td.centered (count solved)]
                      [:td (following-checkbox user-id following _id user)]])
                   user-set)])))

(def-page all-users-page []
  {:title "All 4Clojure Users"
   :content
   (content-page
    {:heading "All 4Clojure Users"
     :sub-heading (list [:span.contributor "*"] "&nbsp;" (link-to repo-url "4clojure contributor"))
     :main (generate-user-list (get-ranked-users))})})

(def-page top-users-page []
  (let [username (session/session-get :user)
        {:keys [user-ranking top-100]} (get-top-100-and-current-user username)]
    {:title "Top 100 Users"
     :content
     (content-page
      {:heading "Top 100 Users"
       :heading-note (list "[show " (link-to "/users/all" "all") "]")
       :sub-heading (list (format-user-ranking user-ranking)
                          [:span.contributor "*"] "&nbsp;"
                          (link-to repo-url "4clojure contributor"))
       :main (generate-user-list top-100)})}))

;; TODO: this is snagged from problems.clj but can't be imported due to cyclic dependency, must refactor this out.
(defn get-problems
  ([]
     (from-mongo
      (fetch :problems
             :only  [:_id :difficulty]
             :where {:approved true}
             :sort  {:_id 1})))
  ([difficulty]
     (get (group-by :difficulty (get-problems)) difficulty [{}])))

(defn get-solved
  ([username]
     (:solved (get-user username)))
  ([username difficulty]
     (let [ids (->> (from-mongo
                     (fetch :problems
                            :only  [:_id]
                            :where {:approved true, :difficulty difficulty}))
                    (map :_id)
                    (set))]
       (filter ids (get-solved username)))))

(def-page user-profile [username]
  (let [page-title (str "User: " username)
        user-id (:_id (get-user username))]
    {:title page-title
     :content
     (list
      [:div.user-profile-name page-title]
      (if (session/session-get :user)
        (with-user [{:keys [_id following]}]
          (if (not= _id user-id)
            (let [[url label] (if (some #{user-id} following)
                                ["unfollow" "Unfollow"]
                                ["follow"   "Follow"])]
              (form-to [:post (str "/user/" url "/" username)]
                [:button.user-follow-button {:type "submit"} label]))
            [:div {:style "clear: right; margin-bottom: 10px;"} "&nbsp;"]))
        [:div {:style "clear: right; margin-bottom: 10px;"} "&nbsp;"])
      [:hr]
      [:table
       (for [difficulty ["Elementary" "Easy" "Medium" "Hard"]]
         (let [solved (count (get-solved username difficulty))
               total  (count (get-problems difficulty))]
           [:tr
            [:td.count-label difficulty]
            [:td.count-value
             [:div.progress-bar-bg
              [:div.progress-bar
               {:style (str "width: "
                            (int (* 100 (/ solved total)))
                            "%")}]]]]))
       [:tr
        [:td.count-total "TOTAL:"    ]
        [:td.count-value
         (count (get-solved username)) "/"
         (count (get-problems))]]])}))

(defn follow-user [username follow?]
  (with-user [{:keys [_id]}]
    (let [follow-id (:_id (get-user username))
          operation (if follow? :$addToSet :$pull)]
      (update! :users
               {:_id _id}
               {operation {:following follow-id}}))))

(defn static-follow-user [username follow?]
  (follow-user username follow?)
  (response/redirect (str "/user/" username)))

(defn rest-follow-user [username follow?]
  (follow-user username follow?)
  (json-str {"following" follow?
             "next-action" (follow-url username (not follow?))
             "next-label" (if follow? "Unfollow" "Follow")}))

(defn set-disable-codebox [disable-flag]
  (with-user [{:keys [_id]}]
    (update! :users
             {:_id _id}
             {:$set {:disable-code-box (boolean disable-flag)}})
    (response/redirect "/problems")))

(defn set-hide-solutions [hide-flag]
  (with-user [{:keys [_id]}]
    (update! :users
             {:_id _id}
             {:$set {:hide-solutions (boolean hide-flag)}})
    (response/redirect "/problems")))

(defroutes users-routes
  (GET  "/users" [] (top-users-page))
  (GET  "/users/all" [] (all-users-page))
  (GET  "/user/:username" [username] (user-profile username))
  (POST "/user/follow/:username" [username] (static-follow-user username true))
  (POST "/user/unfollow/:username" [username] (static-follow-user username false))
  (POST "/rest/user/follow/:username" [username] (rest-follow-user username true))
  (POST "/rest/user/unfollow/:username" [username] (rest-follow-user username false)))
