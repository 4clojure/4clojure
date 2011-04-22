(ns foreclojure.login
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use hiccup.form-helpers
        foreclojure.utils
        compojure.core
        somnium.congomongo)
  (:require [sandbar.stateful-session :as session]
            [ring.util.response :as response])
  (:import (java.net URLEncoder)))
                        
(def-page my-login-page [location]
  [:div.error (session/flash-get :error)]
  (form-to [:post "/login"]
    [:table
     [:tr
      [:td (label :user "Username")]
      [:td (text-field :user)]]
     [:tr
      [:td (label :pwd "Password")]
      [:td (password-field :pwd)]
      (when location
        (hidden-field :location location))]
     [:tr
      [:td (submit-button {:type "image" :src "/login.png"}
                          "Log In")]]]))

(defn do-login [user pwd location]
  (let [{db-pwd :pwd} (from-mongo (fetch-one :users :where {:user (.toLowerCase user)}))]
    (if (and db-pwd (.checkPassword (StrongPasswordEncryptor.) pwd db-pwd))
      (do (update! :users {:user user} {:$set {:last-login (java.util.Date.)}})
          (session/session-put! :user user)
          (response/redirect (or location "/problems")))
      (flash-error "Error logging in."
                   (apply str "/login"
                          (when location
                            ["?location=" (URLEncoder/encode location)]))))))

(defroutes login-routes
  (GET  "/login" {{:strs [location]} :params}
        (my-login-page location))
  (POST "/login" {{:strs [user pwd location]} :form-params}
        (do-login user pwd location))
  (GET "/logout" []
       (do (session/session-delete-key! :user)
           (response/redirect "/"))))
