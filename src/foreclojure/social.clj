(ns foreclojure.social
  (:use foreclojure.utils
        compojure.core
        hiccup.page-helpers
        somnium.congomongo)
  (:require [clj-github.gists :as gist]
            [sandbar.stateful-session :as session])
  (:import java.net.URLEncoder))

(defn throttled
  "Create a version of a function which 'refuses' to be called too
  frequently. If it has successfully been called in the last N milliseconds,
  calls to it will return nil; if no calls have succeeded in that period, args
  will be passed along to the base function."
  [f ms-period]
  (let [tracker (atom {:last-sent 0})]
    (fn [& args]
      (when (:accepted (swap! tracker
                              (fn [{:keys [last-sent]}]
                                (let [now (System/currentTimeMillis)
                                      ok (< ms-period (- now last-sent))]
                                  {:accepted ok
                                   :last-sent (if ok now last-sent)}))))
        (apply f args)))))

(def clojure-hashtag (throttled (constantly "#clojure ")
                                (* 1000 60 60))) ; hourly

(defn tweet-link [id status & [anchor-text]]
  (str "<a href=\"http://twitter.com/share?"
       "text=" (URLEncoder/encode status)
       "&url=" (URLEncoder/encode
	        (str "https://4clojure.com/problem/" id))
       "&related=4clojure"
       "\">"
       (or anchor-text "Twitter")
       "</a>"))

(defn gist!
  "Create a new gist containing a user's solution to a problem and
  return its url."
  [user-name problem-num solution]
  (let [user-name (or user-name "anonymous")
        {name :title} (fetch-one :problems
                                 :where {:_id problem-num})
        filename (str user-name "-4clojure-solution" problem-num ".clj")
        text (str ";; " user-name "'s solution to " name "\n"
                  ";; https://4clojure.com/problem/" problem-num
                  "\n\n"
                  solution)]
    (try
      (->> (gist/new-gist {} filename text)
           :repo
           (str "https://gist.github.com/"))
      (catch Throwable _))))

(defn tweet-solution [id gist-url & [link-text]]
  (let [status-msg (str "Check out how I solved problem #"
                        id " on #4clojure " (clojure-hashtag) gist-url)]
    (tweet-link id status-msg link-text)))

(def-page share-page []
  (if-let [[id code] (session/session-get :code)]
    (let [user (session/session-get :user)
          gist-url (gist! user id code)
          gist-link (if gist-url
                      [:div {:id "shared-code-box"}
                       [:div.code
                        [:h3 "Your Solution"]
                        [:pre {:class "brush: clojure;gutter: false;toolbar: false;light: true"} code]]
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
