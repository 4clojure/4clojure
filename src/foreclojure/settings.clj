(ns foreclojure.settings
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form-helpers      :only [form-to label text-field password-field check-box]]
            [foreclojure.utils        :only [from-mongo flash-error flash-msg with-user form-row assuming send-email login-url]]
            [foreclojure.template     :only [def-page content-page]]
            [foreclojure.users        :only [disable-codebox? hide-solutions? gravatar-img]]
            [compojure.core           :only [defroutes GET POST]]
            [useful.map               :only [keyed]]
            [clojail.core             :only [thunk-timeout]]
            [clojure.stacktrace       :only [print-cause-trace]]
            [somnium.congomongo       :only [update! fetch-one]]))

(defn account-settings-box [user email]
  (list
    [:p "Leave new and reset password fields blank if you do not wish to change your password."]
    [:table
     (map form-row
          [[text-field :new-username "Username" user]
           [password-field :old-pwd "Current password"]
           [password-field :pwd "New password"]
           [password-field :repeat-pwd "Repeat password"]
           [text-field :email "Email" email]])]))

(defn js-settings-box [user-obj]
  (list
   [:p "Selecting this will disable the JavaScript code entry box and just give you plain text entry"]
   (check-box :disable-codebox
              (disable-codebox? user-obj))
   [:label {:for "disable-codebox"}
    "Disable JavaScript in code entry box"]
   [:br]))

(defn hide-settings-box [user-obj]
  (list
   [:p "When you solve a problem, we allow any user who has solved a problem to view your solutions to that problem. Check this box to keep your solutions private."]
   (check-box :hide-solutions
              (hide-solutions? user-obj))
   [:label {:for "hide-solutions"}
    "Hide my solutions"]
   [:br]))

(def-page settings-page []
  (with-user [{:keys [user email] :as user-obj}]
    {:title "Account settings"
     :content
     (content-page
      {:main
        (form-to [:post "/settings"]
         (list
          [:h2 "Change settings for " user]
          [:div#account-settings (account-settings-box user email)]
          [:hr]
          [:h3 "Disable JavaScript Code Box"]
          [:div#settings-codebox (js-settings-box user-obj)]
          [:hr]
          [:h3 "Hide My Solutions"]
          [:div#settings-follow (hide-settings-box user-obj)]
          [:hr]
          [:h3 "Profile Image"]
          [:div (gravatar-img {:email email :size 64})]
          [:p "To change your profile image, visit <a href='http://gravatar.com' target='_blank'>Gravatar</a> and edit the image for '" email "'."]
          [:div#button-div
            [:button {:type "submit"} "Submit"]]))})}))

(defn do-update-settings! [new-username old-pwd new-pwd repeat-pwd email disable-codebox hide-solutions]
  (with-user [{:keys [user pwd]}]
    (let [encryptor (StrongPasswordEncryptor.)
          new-pwd-hash (.encryptPassword encryptor new-pwd)
          new-lower-user (.toLowerCase new-username)]
      (assuming [(or (= new-lower-user user) (nil? (fetch-one :users :where {:user new-lower-user})))
                 "User already exists",
                 (< 3 (.length new-lower-user) 14)
                 "Username must be 4-13 characters long",
                 (= new-lower-user
                    (first (re-seq #"[A-Za-z0-9_]+" new-lower-user)))
                 "Username must be alphanumeric"
                 (or (empty? new-pwd) (< 6 (.length new-pwd)))
                 "New password must be at least seven characters long",
                 (= new-pwd repeat-pwd)
                 "New password was not entered identically twice"
                 (or (empty? new-pwd)
                     (.checkPassword encryptor old-pwd pwd))
                 "Current password incorrect"
                 (re-find #"^\.+@\S+\.\S{2,4}$" email)  
                 "Please enter a valid email address"
                 (nil? (fetch-one :users :where {:email email :user {:$ne user}}))
                 "User with this email address already exists"]
          (do
            (update! :users {:user user}
                     {:$set {:pwd (if (not-empty new-pwd) new-pwd-hash pwd) :user new-lower-user :email email
                             :disable-code-box (boolean disable-codebox) :hide-solutions (boolean hide-solutions)}}
                     :upsert false)
            (session/session-put! :user new-lower-user)
            (flash-msg "/problems"
              (str "Account for " new-lower-user " updated successfully")))
          (flash-error "/settings" why)))))

(defroutes settings-routes
  (GET  "/settings" [] (settings-page))
  (POST "/settings" {{:strs [new-username old-pwd pwd repeat-pwd email disable-codebox hide-solutions]} :form-params}
    (do-update-settings! new-username old-pwd pwd repeat-pwd email disable-codebox hide-solutions)))
