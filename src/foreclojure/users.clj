(ns foreclojure.users
  (:use [foreclojure.utils]
        [somnium.congomongo])) 

(defn get-users []
  (from-mongo
   (fetch :users
          :only [:user :solved])))

(def-page users-page []
  [:table {:class "my-table" :width "50%"}
   [:th {:width "66%"} "Username"]
   [:th "Problems Solved"]
   (map-indexed #(vec [:tr (row-class %1)
                       [:td (:user %2)]
                       [:td {:class "centered"} (count (:solved %2))]])
                (get-users))])

