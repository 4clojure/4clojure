(ns foreclojure.utils
  (:use (hiccup [core :only [html]]
                [page-helpers :only [doctype include-css]])
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
     (include-css "/style.css")]
    [:body
     [:div {:id "top"} [:img {:id "logo" :src "/logo.png"}]]
     [:div {:id "content"}
      [:div {:id "menu"}
       [:a {:href "/"} "Main Page"]
       [:a {:href "/problems"} "Problem List"]
       [:a {:href "/users"} "Top Users"]
       [:a {:href "/directions"} "Getting Started"]
        [:span {:id "user-info"}
       (if-let [user (session/session-get :user)]
         [:div
          (str "Logged in as " user)
          [:a {:id "logout" :href "/logout"} "Logout"]]
         [:div
          [:a {:href "/login"} "Login"] " or "
          [:a {:href "/register"} "Register"]])]]
      [:div {:id "content"} body]
      [:div {:id "footer"} "&copy; 2011 David Byrne" ]]]]))
