(ns foreclojure.golf
  (:use hiccup.form-helpers
        hiccup.page-helpers
        [foreclojure utils config users]
        compojure.core
        somnium.congomongo)
  (:require [ring.util.response :as response]
            [sandbar.stateful-session :as session]))

(def-page golfer-page []
  "Your preferences have been saved.")

(def-page opt-in-page []
  (with-user [user-obj]
    [:div
     [:h2 "League sign-up"]
     [:div#explain "While the primary purpose of 4clojure.com is to teach Clojure \"by doing\", you may also choose to compete for the shortest solution. This is affectionately known as " (link-to "http://lbrandy.com/blog/2008/09/what-code-golf-taught-me-about-python/" "code golf") ": the lower your score the better, get it? If you choose to participate, we'll score your correct solutions based on the number of non-whitespace characters (and some more metrics in the future). We'll also provide a chart showing how you stack up compared to everyone else on the site."]
     [:table
      (form-to [:post "/golf/opt-in"]
        [:tr
         [:td
          (check-box :opt-in
                     (golfer? user-obj))
          [:label {:for "opt-in"} 
           "I want to join the golf league and compete to find the shortest solutions"]]]
        [:tr [:td [:button {:type "submit"} "Update"]]])]]))

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
    (golfer-page))

  (GET "/league" []
    (comment ;; be smarter somehow in future. not sure what the right UI is, atm
     (if-let [username (session/session-get :user)]
       (let [user-obj (get-user username)]
         (if-not (golfer? user-obj)
           (response/redirect "/leag")))))
    (opt-in-page))

  (GET "/league/opt-in" []
    (opt-in-page)))
