(ns foreclojure.login
  (:require [noir.session             :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form              :only [form-to label text-field password-field check-box]]
            [foreclojure.utils        :only [from-mongo flash-error flash-msg form-row assuming send-email login-url decode-url]]
            [foreclojure.template     :only [def-page content-page]]
            [foreclojure.messages     :only [err-msg]]
            [compojure.core           :only [defroutes GET POST]]
            [useful.map               :only [keyed]]
            [clojail.core             :only [thunk-timeout]]
            [clojure.stacktrace       :only [print-cause-trace]]
            [somnium.congomongo       :only [update! fetch-one]]))

(def password-reset-url "https://www.4clojure.com/settings")

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
    (let [sanitized-location (sanitize-url location)] 
      (if sanitized-location  (session/put! :login-to sanitized-location))) 
    {:title "4clojure - login"
     :content
     (content-page
       {:main login-box})}))

(defn do-login [user pwd]
  (let [user (.toLowerCase user)
        {db-pwd :pwd} (from-mongo (fetch-one :users :where {:user user}))
        location (session/get :login-to)]
    (if (and db-pwd (.checkPassword (StrongPasswordEncryptor.) pwd db-pwd))
      (do (update! :users {:user user}
                   {:$set {:last-login (java.util.Date.)}}
                   :upsert false) ; never create new users accidentally
          (session/put! :user user)
          (session/remove! :login-to)
          (response/redirect (or location "/problems")))
      (flash-error "/login" "Error logging in."))))

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
                " soon at " password-reset-url " - pick"
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
        (do (session/put! :login-to password-reset-url)
            (flash-msg (login-url password-reset-url)
              "Your password has been reset! You should receive an email soon."))
        (do (spit (str name ".pwd") diagnostics)
            (flash-error "/login/reset"
              (err-msg "security.err-pwd-email" name)))))
    (flash-error "/login/reset"
      (err-msg "security.err-unknown"))))

(defroutes login-routes
  (GET  "/login" [location] (my-login-page location))
  (POST "/login" {{:strs [user pwd]} :form-params}
    (do-login user pwd))

  (GET  "/login/reset" [] (reset-password-page))
  (POST "/login/reset" [email]
    (do-reset-password! email))

  (GET "/logout" []
    (do (session/remove! :user)
        (response/redirect "/"))))
