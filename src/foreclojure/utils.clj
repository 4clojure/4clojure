(ns foreclojure.utils
  (:use (hiccup [core :only [html]]
                [page-helpers :only [doctype include-css]]))
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])))

(defmacro dbg[x] `(let [x# ~x] (println '~x "=" x#) x#))

(defn flash-error [msg url]
  (do (session/flash-put! :error msg)
      (response/redirect url)))

(defn flash-msg [msg url]
  (do (session/flash-put! :message msg)
      (response/redirect url)))

(defmacro def-page [page-name [& args] & code]
  `(defn ~page-name [~@args]
     (html-doc
      ~@code)))

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
     [:div {:id "header"}
      [:img {:id "logo" :src "/logo.png"}]
      [:div {:id "user-info"}
       (if-let [user (session/session-get :user)]
         [:div
          (str "Logged in as " user)
          [:a {:id "logout" :href "/logout"} "Logout"]]
         [:div
          [:a {:href "/login"} "Login"] " or "
          [:a {:href "/register"} "Register"]])]]
     [:div {:id "menu"}
      [:ul
       [:li [:a {:href "/"} "Main Page"]]
       [:li [:a {:href "/problems"} "Problem List"]]
       [:li [:a {:href "/users"} "Top Users"]]
       [:li [:a {:href "/directions"} "Getting Started"]]
       [:li [:a {:href "/links"} "Useful Links"]]]
      [:div
       [:img {:src "/PoweredMongoDBbeige50.png"}]]]
     [:div {:id "content"} body]
     [:footer      
      [:span {:id "footer"} "&copy; 2011 David Byrne" ]]]]))