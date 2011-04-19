(ns foreclojure.social
  (:use [foreclojure.utils])
  (:require [clj-github.gists :as gist])
  (:import (java.net URLEncoder)))

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
