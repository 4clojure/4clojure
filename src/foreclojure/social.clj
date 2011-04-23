(ns foreclojure.social
  (:use foreclojure.utils
        compojure.core
        hiccup.page-helpers)
  (:require [clj-github.gists :as gist]
            [sandbar.stateful-session :as session])
  (:import java.net.URLEncoder))

(defn tweet-link [status & [anchor-text]]
  (str "<a href=\"http://twitter.com/home?status="
       (URLEncoder/encode status) "\">"
       (or anchor-text "Twitter")
       "</a>"))

(defn gist!
  "Create a new gist containing a user's solution to a problem and
  return its url."
  [user-name problem-num solution]
  (let [user-name (or user-name "anonymous")
        filename (str user-name "-4clojure-solution" problem-num ".clj")
        text (str ";; " user-name
                  "'s solution to http://4clojure.com/problem/" problem-num
                  "\n\n"
                  solution)]
    (try
      (->> (gist/new-gist {} filename text)
           :repo
           (str "https://gist.github.com/"))
      (catch Throwable _ nil))))

(defn tweet-solution [id gist-url & [link-text]]
  (let [status-msg (str "Check out how I solved http://4clojure.com/problem/"
                        id " - " gist-url " #clojure #4clojure")]
    (tweet-link status-msg link-text)))

(def-page share-page []
  (if-let [[id code] (session/session-get :code)]
    (let [user (session/session-get :user)
          gist-url (gist! user id code)
          gist-link (if gist-url
                      [:div
                       [:div.code
                        [:h3 "Your Solution"]
                        [:pre code]]
                       [:br]
                       [:div.share
                         "Share this " (link-to gist-url "solution")
                         " on " (tweet-solution id gist-url) "?"]]
                      [:div.error
                       "Failed to create gist of your solution"])]
      gist-link)
    [:div.error
     "Sorry...I don't remember you solving anything recently!"]))

(defroutes social-routes
  (GET "/share/code" [] (share-page)))
