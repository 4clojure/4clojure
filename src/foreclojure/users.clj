(ns foreclojure.users
  (:use [foreclojure.utils   :only [from-mongo get-user def-page row-class]]
        [foreclojure.config  :only [config repo-url]]
        [somnium.congomongo  :only [fetch-one fetch]]
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

(defn golfer? [user]
  (some user golfer-tags))

(defn email-address [username]
  (:email (fetch-one :users :where {:user username})))

(defn mailto [username]
  (link-to (str "mailto:" (email-address username))
           username))

(def-page users-page []
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
                    [:a#user-profile-link {:href (str "/user/" user)} user]]
                   [:td.centered (count solved)]])
                (get-users))])

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
    [:h2 "User: " username]
    [:hr]
    [:table
     (for [difficulty ["Elementary" "Easy" "Medium" "Hard"]]
       [:tr [:td.count-label difficulty] [:td.count-value [:div.progress-bar-bg [:div.progress-bar {:style (str "width: " (* 100 (/ (count (get-solved username difficulty)) (count (get-problems difficulty)))) "%")}]]]])
     [:tr [:td.count-total "TOTAL:"    ] [:td.count-value (count (get-solved username)) "/" (count (get-problems))]]])

(defroutes users-routes
  (GET "/users" [] (users-page))
  (GET "/user/:username" [username] (user-profile username)))
