(ns foreclojure.users
  (:use foreclojure.utils
        somnium.congomongo
        compojure.core)) 

(defn get-users []
  (let [users (from-mongo
               (fetch :users
                      :only [:user :solved]))
        sortfn  #(compare (count (:solved %1)) (count (:solved %2)))]
    (reverse (sort sortfn users))))

(def-page users-page []
  [:table {:class "my-table" :width "50%"}
   [:th {:width "66%"} "Username"]
   [:th "Problems Solved"]
   (map-indexed #(vec [:tr (row-class %1)
                       [:td (:user %2)]
                       [:td {:class "centered"} (count (:solved %2))]])
                (get-users))])

(defroutes users-routes
  (GET "/users" [] (users-page)))
