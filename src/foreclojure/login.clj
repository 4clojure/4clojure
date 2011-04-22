(ns foreclojure.login
  (:import org.jasypt.util.password.StrongPasswordEncryptor)
  (:use hiccup.form-helpers
        hiccup.page-helpers
        foreclojure.utils
        compojure.core
        [amalloy.utils :only [rand-in-range]]
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
      (do (update! :users {:user user} {:$set {:last-login (java.util.Date.)}})
          (session/session-put! :user user)
          (response/redirect "/problems"))
      (flash-error "Error logging in." "/login"))))

(def-page update-password-page []
  (with-user [{:keys [user]}]
    [:div#update-pwd
     [:h2 "Change password for " user]
     [:span.error (session/flash-get :error)]
     [:table
      (form-to [:post "/login/update"]
        (map form-row
             [[password-field :old-pwd "Current password"]
              [password-field :pwd "New password"]
              [password-field :repeat-pwd "Repeat password"]])
        [:tr
         [:td (submit-button "Reset now")]])]]))

(defn do-update-password! [old-pwd new-pwd repeat-pwd]
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
           [:div#update-succeeded "Password for " user " updated successfully"]))
        (flash-error why "/login/update")))))

(def-page reset-password-page []
  [:div
   [:div#reset-help
    [:h3 "Forgot your password?"]
    [:div "Enter your email address and we'll send you a new password."]
    [:div
     [:span.error (session/flash-get :error)]
     (form-to [:post "/login/reset"]
       (label :email "Email")
       (text-field :email)
       (submit-button "Reset!"))]]])

(def pw-chars "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXY1234567890")

(defn do-reset-password! [email]
  (if-let [{id :_id, name :user} (fetch-one :users
                                            :where {:email email}
                                            :only [:_id :user])]
    (let [pw (apply str
                    (repeatedly 10 #(rand-nth pw-chars)))
          pw-hash (.encryptPassword (StrongPasswordEncryptor.) pw)]
      (update! :users
               {:_id id}
               {:$set {:pwd pw-hash}})
      (let [send-email (fn [& args]
                         (spit (str "/tmp/email" pw)
                               args))]
        (send-email
         {:From "team@4clojure.com"
          :To email
          :Subject "Password reset"}
         (str "The password for your 4clojure.com account "
              name " has been reset to " pw ". Make sure to change it"
              " soon at https://4clojure.com/login/update - pick"
              " something you'll remember!")))
      (flash-msg "Your password has been reset! You should receive an email soon"
                 "/login"))
    (flash-error "We don't know anyone with that email address!"
                 "/login/reset")))

(defroutes login-routes
  (GET  "/login" [] (my-login-page))
  (POST "/login" {{:strs [user pwd]} :form-params}
    (do-login user pwd))

  (GET  "/login/update" [] (update-password-page))
  (POST "/login/update" {{:strs [old-pwd pwd repeat-pwd]} :form-params}
    (do-update-password! old-pwd pwd repeat-pwd))

  (GET  "/login/reset" [] (reset-password-page))
  (POST "/login/reset" [email]
    (do-reset-password! email))
  
  (GET "/logout" []
    (do (session/session-delete-key! :user)
        (response/redirect "/"))))
