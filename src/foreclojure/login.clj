(ns foreclojure.login
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use hiccup.form-helpers
        foreclojure.utils
        compojure.core
        somnium.congomongo)
  (:require [sandbar.stateful-session :as session]
            [ring.util.response :as response]))
                        
(def-page my-login-page []
  [:div.error (session/flash-get :error)]
  (form-to [:post "/login"]
           [:table
            [:tr
             [:td (label :user "Username")]
             [:td (text-field :user)]]
            [:tr
             [:td (label :pwd "Password")]
             [:td (password-field :pwd)]]
            [:tr
             [:td (submit-button {:type "image" :src "/login.png"}
                                 "Log In")]]]))

(defn do-login [user pwd]
  (let [{db-pwd :pwd} (from-mongo (fetch-one :users :where {:user user}))]
    (if (and db-pwd (.checkPassword (StrongPasswordEncryptor.) pwd db-pwd))
      (do (session/session-put! :user user)
          (response/redirect "/problems"))
      (flash-error "Error logging in." "/login"))))

(defroutes login-routes
  (GET  "/login" [] (my-login-page))
  (POST "/login" {{:strs [user pwd]} :form-params}
        (do-login user pwd))
  (GET "/logout" []
       (do (session/session-delete-key! :user)
           (response/redirect "/"))))
