(ns foreclojure.utils
  (:use (hiccup [core :only [html]]
                [page-helpers :only [doctype include-css
                                     javascript-tag link-to include-js]]
                [form-helpers :only [label]])
        [amalloy.utils.transform :only [transform-if]]
        somnium.congomongo)
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])
            [clojure.walk :as walk])
  (:import java.net.URLEncoder))

(def ^{:dynamic true} *url* nil)

(defn wrap-uri-binding [handler]
  (fn [req]
    (binding [*url* (:uri req)]
      (handler req))))

(defmacro dbg [x]
  `(let [x# ~x] (println '~x "=" x#) x#))

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

(defn flash-fn [type]
  (fn [msg url]
    (session/flash-put! type msg)
    (response/redirect url)))

(def flash-error (flash-fn :error))
(def flash-msg (flash-fn :message))

(defmacro def-page [page-name [& args] & code]
  `(defn ~page-name [~@args]
     (html-doc
      ~@code)))

(defn from-mongo [data]
  (walk/postwalk (transform-if float? int)
                 data))

(defn get-user [username]
  (from-mongo
   (fetch-one :users :where {:user username})))

(defmacro with-user [[user-binding] & body]
  `(if-let [username# (session/session-get :user)]
     (let [~user-binding (get-user username#)]
       ~@body)
     [:span.error "You must " (login-link) " to do this."]))

(defn form-row [[type name info]]
  [:tr
   [:td (label name info)]
   [:td (type name)]])

(defn row-class [x]
  {:class (if (even? x)
            "evenrow"
            "oddrow")})
  
(defn html-doc [& body] 
  (html 
   (doctype :html5)
   [:html 
    [:head 
     [:title "4Clojure"]
     [:link {:rel "alternate" :type "application/atom+xml" :title "Atom" :href "http://4clojure.com/problems/rss"}]
     [:link {:rel "shortcut icon" :href "/favicon.ico"}]
     (include-js "/script/jquery-1.5.2.min.js" "/script/jquery.dataTables.min.js")
     (include-js "/script/foreclojure.js")
     (include-js "/script/xregexp.js" "/script/shCore.js" "/script/shBrushClojure.js")
     (include-js "/script/ace/src/ace.js" "/script/ace/src/theme-textmate.js" "/script/ace/src/mode-clojure.js")
     (include-css "/css/style.css" "/css/demo_table.css" "/css/shCore.css" "/css/shThemeDefault.css")
     [:style {:type "text/css"}
      ".syntaxhighlighter { overflow-y: hidden !important; }"]]
     [:script {:type "text/javascript"} "SyntaxHighlighter.all()"]
    [:body
     [:div#top
      [:a {:href "/"} [:img#logo {:src "/images/logo.png"}]]]
     
     [:div#content
      (if  (session/session-get :user)
        [:div#account
         [:a {:href "/login/update"} "Account Settings"]])
      [:br]
      [:div#menu
       [:a.menu {:href "/"} "Main Page"]
       [:a.menu {:href "/problems"} "Problem List"]
       [:a.menu {:href "/users"} "Top Users"]
       [:a.menu {:href "/directions"} "Getting Started"]
       [:a.menu {:href "http://try-clojure.org"} "REPL"]
       [:a.menu {:href "http://clojuredocs.org"} "Docs"]
       [:span#user-info
        (if-let [user (session/session-get :user)]
          [:div
           [:span#username (str "Logged in as " user )]
           [:a#logout {:href "/logout"} "Logout"]]
          [:div
           [:a#login {:href (login-url)} "Login"]
           [:a#register {:href "/register"} "Register"]])]]
      [:div#content_body body]
      [:div#footer
       "The content on 4clojure.com is available under the EPL v 1.0 license."
       [:a#contact {:href "mailto:team@4clojure.com"} "Contact us!"]]
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
      )]]]))
