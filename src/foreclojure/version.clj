(ns foreclojure.version
  (:use [foreclojure.template :only [def-page]]
        [foreclojure.config   :only [repo-url]]
        [foreclojure.git      :only [sha tag]]
        [compojure.core       :only [defroutes GET]]))

(def-page version []
  {:title "About/version"
   :content
   (if tag
     [:p
      [:a {:href (str repo-url "/tree/" sha)} tag]]
     [:p "No git repository found"])})

(defroutes version-routes
  (GET ["/about/version"] [] (version)))
