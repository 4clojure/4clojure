(ns foreclojure.utils
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response]
            [clojure.walk             :as   walk])
  (:import  [java.net                 URLEncoder]
            [org.apache.commons.mail  HtmlEmail])
  (:use     [hiccup.core              :only [html]]
            [hiccup.page-helpers      :only [doctype include-css javascript-tag link-to include-js]]
            [hiccup.form-helpers      :only [label]]
            [useful.fn                :only [to-fix]]
            [somnium.congomongo       :only [fetch-one]]
            [foreclojure.config       :only [config repo-url]]))

(def ^{:dynamic true} *url* nil)

(defn wrap-uri-binding [handler]
  (fn [req]
    (binding [*url* (:uri req)]
      (handler req))))

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
      [:img {:src (src src-prop)
             :alt (alt alt-prop)}])))

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
  (fn [msg url]
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

(defn html-doc [body]
  (let [attrs (rendering-info (page-attributes body))
        user (session/session-get :user)]
    (html
     (doctype :html5)
     [:html
      [:head
       [:title (:title attrs)]
       [:link {:rel "alternate" :type "application/atom+xml" :title "Atom" :href "http://4clojure.com/problems/rss"}]
       [:link {:rel "shortcut icon" :href "/favicon.ico"}]
       (include-js "/vendor/script/jquery-1.5.2.min.js" "/vendor/script/jquery.dataTables.min.js")
       (include-js "/script/foreclojure.js")
       (include-js "/vendor/script/xregexp.js" "/vendor/script/shCore.js" "/vendor/script/shBrushClojure.js")
       (include-js "/vendor/script/ace/ace.js" "/vendor/script/ace/mode-clojure.js")
       (include-css "/css/style.css" "/css/demo_table.css" "/css/shCore.css" "/css/shThemeDefault.css")
       [:style {:type "text/css"}
        ".syntaxhighlighter { overflow-y: hidden !important; }"]
       [:script {:type "text/javascript"} "SyntaxHighlighter.all()"]]
      [:body
       (when (:fork-banner attrs)
         [:div#github-banner [:a {:href repo-url
                                  :alt "Fork 4Clojure on Github!"}]])
       [:div#top
        (link-to "/" [:img#logo {:src "/images/logo.png" :alt "4clojure.com"}])]
       [:div#content
        [:br]
        [:div#menu
         (for [[link text & [tabbed]]
               [["/" "Main Page"]
                ["/problems" "Problem List"]
                ["/users" "Top Users"]
                ["/directions" "Help"]
                ["http://try-clojure.org" "REPL" true]
                ["http://clojuredocs.org" "Docs" true]]]
           [:a.menu (assoc (when tabbed {:target "_blank"})
                      :href link)
            text])
         [:div#user-info
          (if user
            [:div
             [:span#username (str "Logged in as " user)]
             [:a#logout {:href "/logout"} "Logout"]]
            [:div
             [:a#login {:href (login-url)} "Login"]
             [:a#register {:href "/register"} "Register"]])]]
        (when user
          [:div#lower-menu
           [:span
            (link-to "/login/update" "Account Settings")]
           (when (:golfing-active config)
             [:span ; deserves its own page, but just make it discoverable for now
              (link-to "/league" "Leagues")])
           (when (approver? user)
             [:span
              (link-to "/problems/unapproved" "View Unapproved Problems")])
           (when (can-submit? user)
             [:span (link-to "/problems/submit" "Submit a Problem")])])
        [:div#content_body (:content attrs)]
        [:div#footer
         "The content on 4clojure.com is available under the EPL v 1.0 license."
         (let [email "team@4clojure.com"]
           [:span
            [:a#contact {:href (str "mailto:" email)} "Contact us"]
            (str " (" email ")")])]
        (javascript-tag
         " var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-22844856-1']);
        _gaq.push(['_trackPageview']);

        (function() {
          var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
          ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
          var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();
"
         )]]])))

(defmacro def-page [page-name [& args] & code]
  `(defn ~page-name [~@args]
     (html-doc (do ~@code))))

(defn form-row [[type name info value]]
  [:tr
   [:td (label name info)]
   [:td (type name value)]])

(defn row-class [x]
  {:class (if (even? x)
            "evenrow"
            "oddrow")})
