(ns foreclojure.golf
  (:use hiccup.form-helpers
        hiccup.page-helpers
        [foreclojure utils config]
        compojure.core
        somnium.congomongo)
  (:require [ring.util.response :as response]))

(def-page golfer-page []
  "Your preferences have been saved.")

(defn set-golfer [opt-in]
  (with-user [{:keys [_id]}]
    (update! :users
             {:_id _id}
             {:$set {:golfer (boolean opt-in)}})
    (response/redirect "/golf/opt-in")))

(defroutes golf-routes
  (POST "/golf/opt-in" [opt-in]
    (set-golfer opt-in))
  (GET "/golf/opt-in" []
    (golfer-page)))