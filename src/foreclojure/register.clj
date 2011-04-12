(ns foreclojure.register
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [foreclojure.utils]
        [somnium.congomongo]
        (hiccup [form-helpers]))
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])))
                        
(def-page register-page []
  [:div {:class "error"} (session/flash-get :error)]
  (form-to [:post "/register"]
           [:table
            [:tr
             [:td (label :user "Username (4-12 chars.)")]
             [:td (text-field :user)]]
            [:tr
             [:td (label :pwd "Password (7-12 chars.)")]
             [:td (password-field :pwd)]]
            [:tr
             [:td (label :repeat-pwd "Repeat Password")]
             [:td (password-field :repeat-pwd)]]
            [:tr
             [:td (label :email "Email")]
             [:td (text-field :email)]]
            [:tr
             [:td (submit-button {:type "image" :src "/register.png"} "Register")]]]))

(defn do-register [user pwd repeat-pwd email]
  (if (nil? (fetch-one :users :where {:user user}))
    (if (and (> (.length user) 3) (< (.length user) 13))
      (if (and (> (.length pwd) 6) (< (.length pwd) 13))
        (if (= pwd repeat-pwd)
          (if (not (empty? email))
            (do (insert! :users    
                         {:user user
                          :pwd (.encryptPassword (StrongPasswordEncryptor.) pwd)
                          :email email})
                (session/session-put! :user user)
                (response/redirect "/"))
            (flash-error "Please enter a valid email address" "/register"))
          (flash-error "Passwords don't match" "/register"))
        (flash-error "Password must be 7-12 characters long" "/register"))
      (flash-error "Username must be 4-12 characters long" "/register"))
    (flash-error "User already exists" "/register")))
