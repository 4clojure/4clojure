(ns foreclojure.version
  (:use foreclojure.utils
        compojure.core
        [clojure.java.shell :only [sh]]))

(def-page version []
  (let [sha (:out (sh "git" "rev-parse" "--verify" "HEAD"))]
    [:p "SHA: "
     [:a {:href (str "http://github.com/dbyrne/4clojure/commit/" sha)} sha]]))

(defroutes version-routes
  (GET ["/about/version"] [] (version)))