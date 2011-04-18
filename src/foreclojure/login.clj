(ns foreclojure.login
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [foreclojure.utils]
        [somnium.congomongo]
        (hiccup [form-helpers]))
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])))
                        
(def-page my-login-page []
  [:div {:class "error"} (session/flash-get :error)]
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
  (if-let [db-user (from-mongo (fetch-one :users :where {:user user}))]
    (if (.checkPassword (StrongPasswordEncryptor.) pwd (db-user :pwd))
      (do (session/session-put! :user user)
          (response/redirect "/problems"))
      (flash-error "Error logging in." "/login"))
    (flash-error "Error logging in." "/login")))
    
  
