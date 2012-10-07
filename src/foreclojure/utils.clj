(ns foreclojure.utils
  (:require [noir.session             :as   session]
            [ring.util.response       :as   response]
            [foreclojure.config       :as   config]
            [foreclojure.messages     :as   msg]
            [clojure.walk             :as   walk]
            [clojure.string           :as   string]
            [foreclojure.git          :as   git]
            [hiccup.page              :as   hiccup])
  (:import  [java.net                 URLEncoder URLDecoder]
            (org.apache.commons.lang  StringEscapeUtils)
            (org.apache.commons.mail  HtmlEmail))
  (:use     [hiccup.core              :only [html]]
            [hiccup.page              :only [doctype]]
            [hiccup.element           :only [link-to]]
            [hiccup.form              :only [label]]
            [useful.fn                :only [to-fix]]
            [somnium.congomongo       :only [fetch-one]]
            [foreclojure.ring-utils   :only [*url* static-url]]
            [foreclojure.config       :only [config repo-url]]))

(defn as-int [s]
  (if (integer? s) s,
      (try (Integer. s)
           (catch Exception _ nil))))

(defn escape-html [s]
  (when s (StringEscapeUtils/escapeHtml s)))
(defn unescape-html [s]
  (when s (StringEscapeUtils/unescapeHtml s)))

(defmacro assuming
  "Guard body with a series of tests. Each clause is a test-expression
  followed by a failure value. Tests will be performed in order; if
  each test succeeds, then body is evaluated. Otherwise, fail-expr is
  evaluated with the symbol 'why bound to the failure value associated
  with the failing test."
  [[& clauses] body & [fail-expr]]
  `(if-let [[~'why]
            (cond
             ~@(mapcat (fn [[test fail-value]]
                         [`(not ~test) [fail-value]])
                       (partition 2 clauses)))]
     ~fail-expr
     ~body))

(defprotocol PageWriter
  "Specify how an object should be converted to the {:title \"foo\" :content
  [:div ...] :baz-attr true} format used by def-page for rendering pages."
  (page-attributes [this]))

(extend-protocol PageWriter
  clojure.lang.IPersistentMap
  ;; Supplied map should be used verbatim
  (page-attributes [this] this)

  Object
  ;; User probably just returned a Hiccup structure; shove it into :content
  (page-attributes [this]
    {:content this})

  nil
  ;; Allow to return nothing at all so Compojure keeps looking
  (page-attributes [this] nil))

(let [defaults {:content nil
                :title "4clojure"
                :fork-banner false}]
  (defn rendering-info [attributes]
    (into defaults attributes)))

(defn maybe-update
  "Acts like clojure.core/update-in, except that if the value being assoc'd in
  is nil, then instead the key is dissoc'd entirely."
  ([m ks f]
     (let [[k & ks] ks
           inner (get m k)
           v (if ks
               (maybe-update inner ks f)
               (f inner))]
       (if v
         (assoc m k v)
         (dissoc m k))))
  ([m ks f & args]
     (maybe-update m ks #(apply f % args))))

(defn is-relative-url? [location]
  (.startsWith (URLDecoder/decode location) "/"))

(defn login-url
  ([] (login-url *url*))
  ([location]
     (str "/login?location=" (URLEncoder/encode location))))

(defn login-link
  ([] (login-link "Log in" *url*))
  ([text] (login-link text *url*))
  ([text location]
     (html
      (link-to (login-url location)
               text))))

(defn plausible-email? [address]
  (re-find #"^.+@\S+\.\S{2,4}$" address))

;; Assuming that it will always need SSL. Will make it more flexible later.
(defn send-email [{:keys [from to subject html text reply-to]}]
  (let [{:keys [host port user pass]} config
        base (doto (HtmlEmail.)
               (.setHostName host)
               (.setSSL true)
               (.setFrom from)
               (.setSubject subject)
               (.setAuthentication user pass))]
    (when html
      (.setHtmlMsg base html))
    (when text
      (.setTextMsg base text))
    (doseq [person to]
      (.addTo base person))
    (doseq [person reply-to]
      (.addReplyTo base person))
    (.send base)))

(defn from-mongo [data]
  (walk/postwalk (to-fix float? int)
                 data))

(defn get-user [username]
  (from-mongo
   (fetch-one :users :where {:user username})))

(defmacro if-user
  "Look for a user with the given username in the database, let-ing it
  to the supplied binding and executing the then clause. If no such user
  can be found, evaluate the else clause.

  username defaults to the current value of (session-get :user) if not
  specified. Callers need not verify that username is non-nil: that is
  done for you before consulting the database."
  ([[user-binding username] then]
     `(if-user ~[user-binding username] ~then nil))
  ([[user-binding username] then else]
     (let [userexpr (or username `(session/get :user))]
       `(let [username# ~userexpr]
          (if-let [~user-binding (and username#
                                      (get-user username#))]
            ~then
            ~else)))))

(defmacro with-user [[binding expr] & body]
  `(if-user [~binding ~expr]
     (do ~@body)
     [:span.error (msg/err-msg "security.login-required" (login-link))]))

(defn flash-fn [type]
  (fn [url msg]
    (session/flash-put! type msg)
    (response/redirect url)))

(def flash-error (flash-fn :error))
(def flash-msg (flash-fn :message))

(defn user-attribute [attr]
  (fn [username]
    (attr (from-mongo
           (fetch-one :users
                      :where {:user username}
                      :only [attr])))))

(def get-solved (comp set (user-attribute :solved)))
(def approver? (user-attribute :approver))

(defn can-submit? [username]
  (or (approver? username)
      (and (:problem-submission config)
           (>= (count (get-solved username))
               (:advanced-user-count config)))))

(defn image-builder
  "Return a function for constructing an [:img] element from a keyword.

  data should be a map from image \"names\" to pairs [src, alt]. The function
  returned by image-builder will look up its argument as an image name, and
  return an img element with the appropriate src and alt attributes.

  Optionally, additional keyword arguments :alt and :src may be supplied to
  image-builder - these functions will be called to transform the alt and src
  attributes of the returned img."
  [data & {:keys [alt src] :or {alt identity, src identity}}]
  (fn [key]
    (let [[src-prop alt-prop] (get data key)]
      [:img {:src (static-url (src src-prop))
             :alt (alt alt-prop)}])))

(defn form-row [[type name info value]]
  [:tr
   [:td (label name info)]
   [:td (type name value)]])

(defn row-class [x]
  {:class (if (even? x)
            "evenrow"
            "oddrow")})

(defn rank-class [x]
  {:class (if (even? x)
            "evenrank"
            "oddrank")})

(def codemirror-themes ["ambiance" "blackboard" "cobalt" "eclipse" "elegant" "erlang-dark"
                        "lesser-dark" "monokai" "neat" "night" "rubyblue" "vibrant-ink" "xq-dark"])

(def default-theme "neat")

(defn get-theme []
  (if-user [{:keys [theme]}]
    (or theme default-theme)
    default-theme))