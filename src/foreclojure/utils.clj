(ns foreclojure.utils
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response]
            [foreclojure.config       :as   config]
            [clojure.walk             :as   walk]
            [clojure.string           :as   string]
            [foreclojure.git          :as   git]
            [hiccup.page-helpers      :as   hiccup])
  (:import  [java.net                 URLEncoder]
            (org.apache.commons.lang  StringEscapeUtils)
            (org.apache.commons.mail  HtmlEmail))
  (:use     [hiccup.core              :only [html]]
            [hiccup.page-helpers      :only [doctype javascript-tag link-to]]
            [hiccup.form-helpers      :only [label]]
            [useful.fn                :only [to-fix]]
            [somnium.congomongo       :only [fetch-one]]
            [foreclojure.config       :only [config repo-url]]))

(def ^{:dynamic true} *url* nil)

(defn wrap-uri-binding [handler]
  (fn [req]
    (binding [*url* (:uri req)]
      (handler req))))

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

(defmacro with-user [[user-binding] & body]
  `(if-let [username# (session/session-get :user)]
     (let [~user-binding (get-user username#)]
       ~@body)
     [:span.error "You must " (login-link) " to do this."]))

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

(let [prefix (str (when-let [host config/static-host]
                    (str "http://" host))
                  "/")]
  (defn static-url [url]
    (str prefix url)))

(let [version-suffix (str "__" git/tag)]
  (defn add-version-number [file]
    (let [[_ path ext] (re-find #"(.*)\.(.*)$" file)]
      (str path version-suffix "." ext)))

  (defn strip-version-number [file]
    (string/replace file version-suffix "")))

(letfn [(wrap-versioning [f]
          (fn [& files]
            (for [file files]
              (f (static-url (add-version-number file))))))]
  (def js  (wrap-versioning hiccup/include-js))
  (def css (wrap-versioning hiccup/include-css)))



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