(ns foreclojure.version
  (:use [foreclojure.utils  :only [def-page]]
        [foreclojure.config :only [repo-url]]
        [foreclojure.git    :only [sha]]
        [compojure.core     :only [defroutes GET]]))

(def-page version []
  {:title "About/version"
   :content
   (if sha
     [:p "SHA: "
      [:a {:href (str repo-url "/tree/" sha)} sha]]
     [:p "No git repository found"])})

(defroutes version-routes
  (GET ["/about/version"] [] (version)))
