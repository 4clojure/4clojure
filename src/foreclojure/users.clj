(ns foreclojure.users
  (:require [ring.util.response       :as   response]
            [sandbar.stateful-session :as      session])
  (:use [foreclojure.utils   :only [from-mongo def-page row-class get-user with-user]]
        [foreclojure.config  :only [config repo-url]]
        [somnium.congomongo  :only [fetch-one fetch update!]]
        [compojure.core      :only [defroutes GET]]
        [hiccup.page-helpers :only [link-to]]))

(def golfer-tags (into [:contributor]
                       (when (:golfing-active config)
                         [:golfer])))

(defn get-user-id [name]
  (:_id
   (fetch-one :users
              :where {:user name}
              :only [:_id])))

(def sort-by-solved-and-date (juxt (comp count :solved) :last-login))

(defn users-sort [users]
  (reverse (sort-by sort-by-solved-and-date users)))

(defn get-users []
  (let [users (from-mongo
               (fetch :users
                      :only [:user :solved :contributor]))
        sortfn  (comp - count :solved)]
    (sort-by sortfn users)))

(defn get-user-with-ranking [username, users]
  (let [users-with-rankings (map-indexed
                             (fn [idx itm]
                               (assoc itm :rank (inc idx))) users) ]
    (first
     (filter #(= username (% :user)) users-with-rankings))))

(defn golfer? [user]
  (some user golfer-tags))

(defn disable-codebox? [user]
  (true? (:disable-code-box user)))

(defn email-address [username]
  (:email (fetch-one :users :where {:user username})))

(defn mailto [username]
  (link-to (str "mailto:" (email-address username))
           username))

(defn format-user-ranking [{:keys [rank user contributor solved]}]
  [:div
   [:h2 "Your Ranking"]
   [:div.ranking (str "Username: ")
     (when contributor [:span.contributor "* "])
                      [:a.user-profile-link {:href (str "/user/" user)} user]]
   [:div.ranking (str "Rank: " rank)]
   [:div.ranking (str "Problems Solved: " (count solved))]
   [:br]
   [:br]])

(defn display-user-ranking []
  (when-let [username  (session/session-get :user)]
    (format-user-ranking
     (get-user-with-ranking username (get-users)))))

(def-page users-page []
  {:title "Top 100 Users"
   :content
   (list
    [:h1 "Top 100 Users"]
     (display-user-ranking)
    [:div
     [:span.contributor "*"] " "
     (link-to repo-url "4clojure contributor")]
    [:br]
    [:table#user-table.my-table
     [:thead
      [:tr
       [:th {:style "width: 40px;"} "Rank"]
       [:th "Username"]
       [:th "Problems Solved"]]]
     (map-indexed (fn [rownum {:keys [user contributor solved]}]
                    [:tr (row-class rownum)
                     [:td (inc rownum)]
                     [:td
                      (when contributor [:span.contributor "* "])
                      [:a.user-profile-link {:href (str "/user/" user)} user]]
                     [:td.centered (count solved)]])
                  (take 100 (get-users)))])})

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
  (let [page-title (str "User: " username)]
    {:title page-title
     :content
     (list
      [:h2 page-title]
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

(defn set-disable-codebox [disable-flag]
  (with-user [{:keys [_id]}]
    (update! :users
             {:_id _id}
             {:$set {:disable-code-box (boolean disable-flag)}})
    (response/redirect "/problems")))

(defroutes users-routes
  (GET "/users" [] (users-page))
  (GET "/user/:username" [username] (user-profile username)))
