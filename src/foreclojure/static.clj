(ns foreclojure.static
  (:use [compojure.core            :only [defroutes GET]]
        [foreclojure.ring-utils    :only [static-url]]
        [foreclojure.template      :only [def-page]]
        [hiccup.form               :only [hidden-field]]))

(def-page help-page []
  {:title "Help"
   :content
   [:div#help-content
    [:div#getting-started
     [:h2 "Getting Started"]
     [:div
      "4Clojure challenges users to solve "
      [:a {:href "http://en.wikipedia.org/wiki/K%C5%8Dan"} "koan-style"]
      " problems.  This is a fancy way of saying you'll be asked to fill in the blanks.  The \"blanks\" that you will be filling in are indicated by a double underscore: '__'.  This is "
      [:u "not"]
      " part of the syntax of the language.  Any code which makes the final form evaluate to 'true' will be considered a correct answer.  Lets consider the first problem:"
      [:br]
      [:pre "(= __ true)"]
      [:br]
      "Any of the following would be considered correct answers:"
      [:br][:br]
      [:li "true"]
      [:li "(= 1 1)"]
      [:li "(nil? nil)"]
      [:br]
      "Some problems will expect you to fill-in-the-blanks with a function.  Here is a problem which asks you to provide a function to double a number:"
      [:br]
      [:pre "(= (__ 2) 4)\n(= (__ 3) 6)\n(= (__ 11) 22)\n(= (__ 7) 14)"]
      [:br]
      "Any of the following forms are valid solutions:"
      [:br][:br]
      [:li "(fn double [x] (* 2 x))"]
      [:li "(fn [x] (* 2 x))"]
      [:li "#(* 2 %)"]
      [:li "(partial * 2)"]
      [:br][:br]
      "Some operations are prohibited for security reasons.  For instance, you will not be able to use \"def\" or switch namespaces.  In addition, some problems have special restrictions.  For example, a function which is supposed to count the number of elements in a sequence will not be allowed to use the \"count\" function.  Obviously, this would defeat the purpose.  Any special restrictions will be listed on the problem page."
      [:br][:br]
      "Many of the easier problems on this site can be solved using only your browser.  However at some point you will want to install Clojure, and use your favorite IDE or text editor to write your code.  We prefer using Emacs, but it's totally a matter of personal preference.  Writing all your code directly on the site has a disadvantage:"
      [:br][:br]
      [:li "4Clojure is not an IDE, and doesn't try to be"]
      [:br]
      "Check out the official Clojure "
      [:a {:href "http://clojure.org/help"} "help"]
      " page for instructions on installation and getting started."
      [:br][:br]
      "You should now be ready to start solving problems.  Happy coding!"]]
    [:h2 "Need help with a problem?"]
    [:p "Do you need some hints or help in solving a problem?  Visit the "
     [:a {:href "http://groups.google.com/group/4clojure"} "4Clojure Google Group"]
     "."]]})

(defroutes static-routes
  (GET "/directions" [] (help-page)))
