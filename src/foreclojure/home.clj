(ns foreclojure.home
  (:require [noir.session          :as   session])
  (:use [compojure.core            :only [defroutes GET]]
        [foreclojure.problems      :only [solved-stats]]
        [foreclojure.config        :only [repo-url]]
        [foreclojure.login         :only [login-box]]
        [foreclojure.template      :only [def-page]]
        [foreclojure.utils         :only [if-user]]
        [hiccup.form               :only [hidden-field]]))

(def df
  (let [df (java.text.DecimalFormat.)
        dfs (java.text.DecimalFormatSymbols.)]
    (.setGroupingSeparator dfs \,)
    (.setDecimalFormatSymbols df dfs)
    df))

(def-page welcome-page []
  {:title "4clojure &ndash; Welcome!"
   :fork-banner true
   :content
   [:div#welcome
    [:div#action
     [:div#totalcount
      (hidden-field :counter-value (:total @solved-stats))
      [:span#totalcounter (.format df (:total @solved-stats))] " problems solved and counting!"]
     [:div (if-user [{:keys [user]}]
       [:p "Go " [:a {:href "/problems" } "solve some problems"] ", " user "!"]
       login-box)]]
    [:div#info
     [:h3 "What is 4Clojure?"]
     [:p "4Clojure is a resource to help fledgling clojurians learn the language through interactive problems.  The first few problems are easy enough that even someone with no prior experience should find the learning curve forgiving.  See 'Help' for more information."]
     [:h3 "Is this site written in Clojure?"]
     "Absolutely!  This site was created using a variety of open source Clojure (and Java) libraries.  In fact, the "
     [:a {:href repo-url} "code for this site"]
     " is itself open source.  Once you've mastered the language, feel free to contribute something back to the community."]]})

(defroutes home-routes
  (GET "/" [] (welcome-page)))
