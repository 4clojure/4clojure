(ns foreclojure.login
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use hiccup.form-helpers
        hiccup.page-helpers
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
             [:td (submit-button {:type "image" :src "/images/login.png"}
                                 "Log In")]]]))

(defn do-login [user pwd]
  (let [{db-pwd :pwd} (from-mongo (fetch-one :users :where {:user (.toLowerCase user)}))]
    (if (and db-pwd (.checkPassword (StrongPasswordEncryptor.) pwd db-pwd))
      (do (session/session-put! :user user)
          (response/redirect "/problems"))
      (flash-error "Error logging in." "/login"))))

(def-page reset-password-page []
  (with-user [{:keys [user]}]
    [:div#reset-pwd
     [:h2 "Reset password for " user]
     [:span.error (session/flash-get :error)]
     [:table
      (form-to [:post "/login/reset"]
        (map form-row
             [[password-field :old-pwd "Current password"]
              [password-field :pwd "New password"]
              [password-field :repeat-pwd "Repeat password"]])
        [:tr
         [:td (submit-button "Reset now")]])]]))

(defn do-reset-password! [old-pwd new-pwd repeat-pwd]
  (with-user [{:keys [user pwd]}]
    (let [encryptor (StrongPasswordEncryptor.)]
      (assuming [(= new-pwd repeat-pwd)
                 "New password was not entered identically twice"
                 (.checkPassword encryptor old-pwd pwd)
                 "Old password incorrect"]
        (let [new-pwd-hash (.encryptPassword encryptor new-pwd)]
          (update! :users {:user user}
                   {:$set {:pwd new-pwd-hash}}
                   :upsert false)
          (html-doc
           [:div#reset-succeeded "Password for " user " reset successfully"]))
        (flash-error why "/login/reset")))))

(defroutes login-routes
  (GET  "/login" [] (my-login-page))
  (POST "/login" {{:strs [user pwd]} :form-params}
    (do-login user pwd))
  (GET  "/login/reset" [] (reset-password-page))
  (POST "/login/reset" {{:strs [old-pwd pwd repeat-pwd]} :form-params}
    (do-reset-password! old-pwd pwd repeat-pwd))
  (GET "/logout" []
    (do (session/session-delete-key! :user)
        (response/redirect "/"))))
