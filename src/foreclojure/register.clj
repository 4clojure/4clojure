(ns foreclojure.register
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use hiccup.form-helpers
        compojure.core
        foreclojure.utils
        somnium.congomongo)
  (:require [sandbar.stateful-session :as session]
            [ring.util.response :as response]))

(def-page register-page []
  [:div.error (session/flash-get :error)]
  (form-to [:post "/register"]
           [:table
            (map form-row
                 [[text-field :user "Username (4-13 chars.)"]
                  [password-field :pwd "Password (7-13 chars.)"]
                  [password-field :repeat-pwd "Repeat Password"]
                  [text-field :email "Email"]])
            [:tr
             [:td [:button {:type "submit"} "Register"]]]]))

(defn do-register [user pwd repeat-pwd email]
  (let [lower-user (.toLowerCase user)]
    (assuming [(nil? (fetch-one :users :where {:user lower-user}))
               "User already exists",
               (< 3 (.length lower-user) 14)
               "Username must be 4-13 characters long",
               (= lower-user
                  (first (re-seq #"[A-Za-z0-9_]+" lower-user)))
               "Username must be alphanumeric"
               (< 6 (.length pwd) 14)
               "Password must be 7-13 characters long",
               (= pwd repeat-pwd)
               "Passwords don't match",
               (not (empty? email))
               "Please enter a valid email address"]
              (do
                (insert! :users
                         {:user lower-user
                          :pwd (.encryptPassword (StrongPasswordEncryptor.) pwd)
                          :email email})
                (session/session-put! :user user)
                (response/redirect "/"))
              (flash-error why "/register"))))

(defroutes register-routes
  (GET  "/register" [] (register-page))
  (POST "/register" {{:strs [user pwd repeat-pwd email]} :form-params}
        (do-register user pwd repeat-pwd email)))
