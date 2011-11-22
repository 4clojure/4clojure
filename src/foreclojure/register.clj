(ns foreclojure.register
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form-helpers      :only [form-to text-field password-field]]
            [compojure.core           :only [defroutes GET POST]]
            [foreclojure.utils        :only [form-row assuming flash-error plausible-email?]]
            [foreclojure.template     :only [def-page]]
            [foreclojure.messages     :only [err-msgs]]
            [somnium.congomongo       :only [insert! fetch-one]]))

(def-page register-page []
  {:title "4clojure &ndash; Register"
   :content
   (list
    [:div.error (session/flash-get :error)]
    (form-to [:post "/register"]
      [:table
       (map form-row
            [[text-field :user "Username (4-13 chars.)" (session/flash-get :user)]
             [password-field :pwd "Password (7+ chars.)"]
             [password-field :repeat-pwd "Repeat Password"]
             [text-field :email "Email" (session/flash-get :email)]])
       [:tr
        [:td [:button {:type "submit"} "Register"]]]]))})

(defn do-register [user pwd repeat-pwd email]
  (let [lower-user (.toLowerCase user)]
    (assuming [(nil? (fetch-one :users :where {:user lower-user}))
               (err-msgs "settings.user-exists"),
               (< 3 (.length lower-user) 14)
               (err-msgs "settings.uname-size"),
               (= lower-user
                  (first (re-seq #"[A-Za-z0-9_]+" lower-user)))
               (err-msgs "settings.uname-alphanum")
               (< 6 (.length pwd))
               (err-msgs "settings.pwd-size"),
               (= pwd repeat-pwd)
               (err-msgs "settings.pwd-match"),
               (plausible-email? email)
               (err-msgs "settings.email-invalid")
               (nil? (fetch-one :users :where {:email email}))
               (err-msgs "settings.email-exists")]
      (do
        (insert! :users
                 {:user lower-user
                  :pwd (.encryptPassword (StrongPasswordEncryptor.) pwd)
                  :email email})
        (session/session-put! :user lower-user)
        (response/redirect "/"))
      (do
        (session/flash-put! :user user)
        (session/flash-put! :email email)
        (flash-error "/register" why)))))

(defroutes register-routes
  (GET  "/register" [] (register-page))
  (POST "/register" {{:strs [user pwd repeat-pwd email]} :form-params}
        (do-register user pwd repeat-pwd email)))
