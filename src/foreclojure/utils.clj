(ns foreclojure.utils
  (:use (hiccup [core :only [html]]
                [page-helpers :only [doctype include-css javascript-tag include-js]])
        [amalloy.utils.transform :only [transform-if]])
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])
            [clojure.walk :as walk]))

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
     (include-js "/script/jquery-1.5.2.min.js" "/script/jquery.dataTables.min.js")
     (include-js "/script/foreclojure.js")
     (include-css "/css/style.css" "/css/demo_table.css")
    ]
    [:body
     [:div#top [:img#logo {:src "/images/logo.png"}]]
     [:div#content 
      [:div#menu
       [:a.menu {:href "/"} "Main Page"]
       [:a.menu {:href "/problems"} "Problem List"]
       [:a.menu {:href "/users"} "Top Users"]
       [:a.menu {:href "/directions"} "Getting Started"]
       [:span#user-info
        (if-let [user (session/session-get :user)]
          [:div
           [:span#username (str "Logged in as " user )]
           [:a#logout {:href "/logout"} "Logout"]]
          [:div
           [:a#login {:href "/login"} "Login"]
           [:a#register {:href "/register"} "Register"]])]]
      [:div#content body]
      [:div#footer "The content on 4clojure.com is available under the EPL v 1.0 license." ]
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
