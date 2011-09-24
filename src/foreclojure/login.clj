(ns foreclojure.login
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form-helpers      :only [form-to label text-field password-field check-box]]
            [foreclojure.utils        :only [from-mongo flash-error flash-msg with-user form-row assuming send-email login-url]]
            [foreclojure.template     :only [def-page content-page]]
            [foreclojure.users        :only [disable-codebox? set-disable-codebox hide-solutions? set-hide-solutions]]
            [compojure.core           :only [defroutes GET POST]]
            [useful.map               :only [keyed]]
            [clojail.core             :only [thunk-timeout]]
            [clojure.stacktrace       :only [print-cause-trace]]
            [somnium.congomongo       :only [update! fetch-one]]))

(def login-box
  (form-to [:post "/login"]
    [:table
     [:tr
      [:td (label :user "Username")]
      [:td (text-field :user)]]
     [:tr
      [:td (label :pwd "Password")]
      [:td (password-field :pwd)]]
     [:tr
      [:td]
      [:td [:button {:type "submit"} "Log In"]]]
     [:tr
      [:td]
      [:td
       [:a {:href "/login/reset"} "Forgot your password?"]]]]))

(def-page my-login-page [location]
  (do
    (if location (session/session-put! :login-to location))
    {:title "4clojure - login"
     :content
     (content-page
      {:main login-box})}))

(defn do-login [user pwd]
  (let [user (.toLowerCase user)
        {db-pwd :pwd} (from-mongo (fetch-one :users :where {:user user}))
        location (session/session-get :login-to)]
    (if (and db-pwd (.checkPassword (StrongPasswordEncryptor.) pwd db-pwd))
      (do (update! :users {:user user}
                   {:$set {:last-login (java.util.Date.)}}
                   :upsert false) ; never create new users accidentally
          (session/session-put! :user user)
          (session/session-delete-key! :login-to)
          (response/redirect (or location "/problems")))
      (flash-error "Error logging in." "/login"))))

(defn account-settings-box [user]
  [:table
   (form-to [:post "/login/update"]
     (map form-row
          [[text-field :new-username "Username" user]
           [password-field :old-pwd "Current password"]
           [password-field :pwd "New password"]
           [password-field :repeat-pwd "Repeat password"]])
     [:tr
      [:td [:button {:type "submit"} "Reset now"]]])])

(defn js-settings-box [user-obj]
  (list
   [:p "Selecting this will disable the JavaScript code entry box and just give you plain text entry"]
   (form-to [:post "/users/set-disable-codebox"]
     (check-box :disable-codebox
                (disable-codebox? user-obj))
     [:label {:for "disable-codebox"}
      "Disable JavaScript in code entry box"]
     [:br]
     [:div#button-div
      [:button {:type "submit"} "Submit"]])))

(defn hide-settings-box [user-obj]
  (list     
   [:p "When you solve a problem, we allow any user who has solved a problem to view your solutions to that problem. Check this box to keep your solutions private."]
   (form-to [:post "/users/set-hide-solutions"]
     (check-box :hide-solutions
                (hide-solutions? user-obj))
     [:label {:for "hide-solutions"}
      "Hide my solutions"]
     [:br]
     [:div#button-div
      [:button {:type "submit"} "Submit"]])))

(def-page update-credentials-page []
  (with-user [{:keys [user] :as user-obj}]
    {:title "Change password"
     :content
     (content-page
      {:main
       (list
        [:h2 "Change password for " user]
        [:div#account-settings (account-settings-box user)]
        [:hr]
        [:h2 "Disable JavaScript Code Box"]
        [:div#settings-codebox (js-settings-box user-obj)]
        [:hr]
        [:h2 "Hide My Solutions"]
        [:div#settings-follow (hide-settings-box user-obj)])})}))

(defn do-update-credentials! [new-username old-pwd new-pwd repeat-pwd]
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
                 (.checkPassword encryptor old-pwd pwd)
                 "Old password incorrect"]
          (do
            (update! :users {:user user}
                     {:$set {:pwd (if (not-empty new-pwd) new-pwd-hash pwd) :user new-lower-user}}
                     :upsert false)
            (session/session-put! :user new-lower-user)
            (flash-msg (str "Account for " new-lower-user " updated successfully")
                       "/problems"))
          (flash-error why "/login/update")))))

(def-page reset-password-page []
  {:title "Reset password"
   :content
   [:div
    [:div#reset-help
     [:h3 "Forgot your password?"]
     [:div "Enter your email address and we'll send you a new password."]
     [:div
      [:span.error (session/flash-get :error)]
      (form-to [:post "/login/reset"]
        (label :email "Email")
        (text-field :email)
        [:button {:type "submit"} "Reset!"])]]]})

(let [pw-chars "abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXY1234567890"]
  (defn random-pwd []
    (let [pw (apply str
                    (repeatedly 10 #(rand-nth pw-chars)))
          hash (.encryptPassword (StrongPasswordEncryptor.) pw)]
      (keyed [pw hash]))))

(defn try-to-email [email name id]
  (let [{:keys [pw hash]} (random-pwd)]
    (try
      (thunk-timeout
       (fn []
         (update! :users
                  {:_id id}
                  {:$set {:pwd hash}})
         (send-email
          {:from "team@4clojure.com"
           :to [email]
           :subject "Password reset"
           :text
           (str "The password for your 4clojure.com account "
                name " has been reset to " pw ". Make sure to change it"
                " soon at https://4clojure.com/login/update - pick"
                " something you'll remember!")})
         {:success true})
       10 :sec)
      (catch Throwable t
        {:success false, :exception t,
         :message (.getMessage t),
         :trace (with-out-str
                  (binding [*err* *out*]
                    (print-cause-trace t)))
         :pw pw, :hash hash}))))

(defn do-reset-password! [email]
  (if-let [{id :_id, name :user} (fetch-one :users
                                            :where {:email email}
                                            :only [:_id :user])]
    (let [{:keys [success] :as diagnostics} (try-to-email email name id)]
      (if success
        (do (session/session-put! :login-to "/login/update")
            (flash-msg "Your password has been reset! You should receive an email soon."
                       (login-url "/login/update")))
        (do (spit (str name ".pwd") diagnostics)
            (flash-error (str "Something went wrong emailing your new password! Please contact <a href='mailto:team@4clojure.com?subject=Password Reset: " name "'>team@4clojure.com</a> - we'll reset it manually and look into the problem. When you do, please mention your username.")
                         "/login/reset"))))
    (flash-error "We don't know anyone with that email address!"
                 "/login/reset")))

(defroutes login-routes
  (GET  "/login" [location] (my-login-page location))
  (POST "/login" {{:strs [user pwd]} :form-params}
    (do-login user pwd))

  (GET  "/login/update" [] (update-credentials-page))
  (POST "/login/update" {{:strs [new-username old-pwd pwd repeat-pwd]} :form-params}
    (do-update-credentials! new-username old-pwd pwd repeat-pwd))

  (GET  "/login/reset" [] (reset-password-page))
  (POST "/login/reset" [email]
    (do-reset-password! email))

  (GET "/logout" []
    (do (session/session-delete-key! :user)
        (response/redirect "/")))

  (POST "/users/set-disable-codebox" [disable-codebox]
        (set-disable-codebox disable-codebox))

  (POST "/users/set-hide-solutions" [hide-solutions]
    (println "POST")
    (set-hide-solutions hide-solutions)))
