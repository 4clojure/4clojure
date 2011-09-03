(ns foreclojure.version
  (:use [foreclojure.utils  :only [def-page]]
        [foreclojure.config :only [repo-url]]
        [compojure.core     :only [defroutes GET]]
        [clojure.java.shell :only [sh]]))

;; fetch this at load time rather than on demand, so that it's accurate even
;; if someone checks out a different revision to poke at without restarting
;; the server (eg to diagnose bugs in a release)
(def sha (not-empty (:out (sh "git" "rev-parse" "--verify" "HEAD"))))

(def-page version []
  (if sha
    [:p "SHA: "
     [:a {:href (str repo-url "/commit/" sha)} sha]]
    [:p "No git repository found"]))

(defroutes version-routes
  (GET ["/about/version"] [] (version)))
