(ns foreclojure.login
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response]
            [clj-openid.core :as openid]
            [clj-openid.helpers :as helpers])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use     [hiccup.form-helpers      :only [form-to label text-field password-field check-box]]
            [foreclojure.utils        :only [from-mongo flash-error flash-msg form-row assuming send-email login-url]]
            [foreclojure.template     :only [def-page content-page]]
            [foreclojure.messages     :only [err-msg]]
            [compojure.core           :only [defroutes GET POST]]
            [useful.map               :only [keyed]]
            [clojail.core             :only [thunk-timeout]]
            [clojure.stacktrace       :only [print-cause-trace]]
            [somnium.congomongo       :only [update! fetch-one]]))

(def password-reset-url "https://www.4clojure.com/settings")

(def openid-callback-url "http://www.4clojure.com/openid-callback")

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
      [:td [:button {:type "submit"} "Log In"]]]]))

(def openid-login-box
  (form-to [:post "/openid-login"]
           [:table
            [:tr
             [:td (label :openid-url "OpenID URL")]
             [:td (text-field "openid-url")]]
            [:tr
             [:td]
             [:td [:button {:type "submit"} "Log In With OpenID"]]]
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
      {:main [:div login-box openid-login-box]})}))

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
        (do (session/session-put! :login-to password-reset-url)
            (flash-msg (login-url password-reset-url)
              "Your password has been reset! You should receive an email soon."))
        (do (spit (str name ".pwd") diagnostics)
            (flash-error "/login/reset"
              (err-msg "security.err-pwd-email" name)))))
    (flash-error "/login/reset"
                 (err-msg "security.err-unknown"))))

(def-page openid-failure [r]
  {:title "OpenID Failure"
   :content (content-page {:main [:div [:p "The OpenID you provided could not be verified.  Please go back and try again."]]})})

(def-page openid-success [r]
  (let [claimed-id (-> r :params :openid.claimed_id)
        user {:openid claimed-id}
        location (session/session-get :login-to)
        db-user (fetch-one :users :where {:user })]
    (update! :users {:user user}
             {:$set {:last-login (java.util.Date.)}})
    (session/session-put! :user user)
    (session/session-delete-key! :login-to)
    (response/redirect (or location "/problems"))))

;; Putting the session info that openid needs in the sandbar session
;; doesn't work.  Thus, I'll make a little hack around that.
(def openid-sessions (atom {})) 

(defn do-openid-login [r]
  (let [openid-url (-> r :form-params (get "openid-url"))
        cookies (get r :cookies)
        redir (openid/redirect openid-url {} openid-callback-url)
        sess (-> cookies (get "ring-session") :value)]
    (swap! openid-sessions #(assoc % sess (:session redir)))
    (dissoc redir :session)))

(defn do-openid-callback [r]
  (if (openid/validate (assoc r :session (merge (get r :session) (get @openid-sessions (-> r :cookies (get "ring-session") :value)))))
    (openid-success r)
    (openid-failure r)))

(defroutes login-routes
  (GET  "/login" [location] (my-login-page location))
  (POST "/login" {{:strs [user pwd]} :form-params}
        (do-login user pwd))
  (POST "/openid-login" [:as r]
        (do-openid-login r))
  (GET "/openid-callback" [:as r]
       (do-openid-callback r))

  (GET  "/login/reset" [] (reset-password-page))
  (POST "/login/reset" [email]
    (do-reset-password! email))

  (GET "/logout" []
    (do (session/session-delete-key! :user)
        (response/redirect "/"))))
