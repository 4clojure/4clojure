(ns foreclojure.static
  (:use [compojure.core       :only [defroutes GET]]
        [foreclojure.problems :only [solved-stats]]
        [foreclojure.config   :only [repo-url]]
        [foreclojure.utils    :only [static-url]]
        [foreclojure.template :only [def-page]]))

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
    [:div#totalcount
     (.format df (:total @solved-stats)) " problems solved and counting!"]
    [:div
     [:h3 "What is 4Clojure?"]
     [:p "4Clojure is a resource to help fledgling clojurians learn the language through interactive problems.  The first few problems are easy enough that even someone with no prior experience should find the learning curve forgiving.  See 'Help' for more information."]]

    [:div
     [:h3 "Is this site written in Clojure?"]
     "Absolutely!  This site was created using a variety of open source Clojure (and Java) libraries.  In fact, the "
     [:a {:href repo-url} "code for this site"]
     " is itself open source.  Once you've mastered the language, feel free to contribute something back to the community."]
    [:div
     [:h3 "So wait, I can't buy cheap real estate here?"]
     [:p "At this time, 4clojure.com does not provide information regarding the sale of foreclosed homes, and has no plans of doing so in the future."]]
    [:img {:src (static-url "/images/PoweredMongoDBbeige50.png")
           :alt "Powered by MongoDB"
           :width 129 :height 61}]]})

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
      [:pre {:class "brush: clojure;gutter: false;toolbar: false"} "(= __ true)"]
      [:br]
      "Any of the following would be considered correct answers:"
      [:br][:br]
      [:li "true"]
      [:li "(= 1 1)"]
      [:li "(nil? nil)"]
      [:br]
      "Some problems will expect you to fill-in-the-blanks with a function.  Here is a problem which asks you to provide a function to double a number:"
      [:br]
      [:pre {:class "brush: clojure;gutter: false;toolbar: false"}
       "(= (__ 2) 4)\n(= (__ 3) 6)\n(= (__ 11) 22)\n(= (__ 7) 14)"]
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
      "Many of the easier problems on this site can be solved using only your browser.  However at some point you will want to install Clojure, and use your favorite IDE or text editor to write your code.  We prefer using Emacs, but it's totally a matter of personal preference.  Writing all your code directly on the site has a few disadvantages:"
      [:br][:br]
      [:li "4Clojure is not an IDE, and doesn't try to be"]
      [:li "4Clojure won't save any of your code for later use"]
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
