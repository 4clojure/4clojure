(ns foreclojure.static
  (:use [foreclojure.utils]))

(def-page links-page []
  [:div
   [:li [:a {:href "http://clojure.org/getting_started"} "Clojure.org: Getting Started"]]
   [:li [:a {:href "http://clojure.org/cheatsheet"} "Clojure.org: Cheatsheet"]]
   [:li [:a {:href "http://try-clojure.org"} "try-clojure.org"]]
   [:li [:a {:href "https://github.com/functional-koans/clojure-koans"} "Clojure Koans"]]
   [:li [:a {:href "http://stackoverflow.com/questions/2285437/a-gentle-tutorial-to-emacs-swank-paredit-for-clojure"}
         "StackOverflow.com: A Gentle Tutorial to Emacs/Swank/Paredit for Clojure"]]])

(def-page welcome-page []
  [:div
   [:div
    [:h3 "What is 4Clojure?"]
    "4Clojure is a resource to help fledgling clojurians learn the language through interactive problems.  The first few problems are easy enough that even someone with no prior experience should find the learning curve forgiving.  See 'Getting Started' for more information."]

   [:div
    [:h3 "Is this site written in Clojure?"]
    "Absolutely!  This site was created using a variety of open source Clojure (and Java) libraries.  In fact, the code for this site is itself open source.  Once you've mastered the language, feel free to contribute something back to the community."]
   [:div
    [:h3 "So wait, I can't buy cheap real estate here?"]
    "At this time, 4clojure.com does not provide information regarding the sale of foreclosed homes, and has no plans of doing so in the future."]])

(def-page getting-started-page []
  [:div
   "4Clojure challenges users to solve "
   [:a {:href "http://en.wikipedia.org/wiki/K%C5%8Dan"} "koan-style"]
   " problems.  This is a fancy way of saying you'll be asked to fill in the blanks.  The \"blanks\" that you will be filling in are indicated by a double underscore: '__'.  This is "
   [:u "not"]
   " part of the syntax of the language.  Any code which makes the final form evaluate to 'true' will be considered a correct answer.  Lets consider the first problem:"
   [:br][:br]
   [:div {:id "testcases"}
    [:li {:class "testcase"} "(= __ true)"]]
   [:br][:br][:br]
   "Any of the following would be considered correct answers:"
   [:br][:br]
   [:li "true"]
   [:li "(= 1 1)"]
   [:li "(nil? nil)"]
   [:br]
   "Some problems will expect you to fill-in-the-blanks with a function.  Here is a problem which asks you to provide a function to double a number:"
   [:br][:br]
   [:div {:id "testcases"}
    [:li {:class "testcase"} "(= (__ 2) 4)"]
    [:li {:class "testcase"} "(= (__ 3) 6)"]]
   [:br][:br][:br][:br]
   "Any of the following forms are valid solutions:"
   [:br][:br]
   [:li "(fn double [x] (* 2 x))"]
   [:li "(fn [x] (* 2 x))"]
   [:li "#(* 2 %)"]
   [:li "(partial * 2)"]
   [:br]
   "Keep in mind that problems which ask for a function might also check your code against secret test cases.  This is to stop users from \"gaming\" the system by writing code which passes the test cases but is against the spirit of the problem."
   [:br][:br]
   "Some operations are prohibited for security reasons.  For instance, you will not be able to use \"def\" or switch namespaces.  In addition, some problems have special restrictions.  For example, a function which is supposed to count the number of elements in a sequence will not be allowed to use the \"count\" function.  Obviously, this would defeat the purpose.  Any special restrictions will be listed on the problem page."
   [:br][:br]
   "Many of the easier problems on this site can be solved using only your browser.  However at some point you will want to install Clojure, and use your favorite IDE or text editor to write your code.  We prefer using Emacs, but its totally a matter of personal preference.  Writing all your code directly on the site has a few disadvantages:"
   [:br][:br]
   [:li "4Clojure is not an IDE, and doesn't try to be"]
   [:li "4Clojure won't save any of your code for later use"]
   [:br]
   "Check out the official "
   [:a {:href "http://clojure.org/getting_started"} "getting started"]
   " page for help with installation."
   [:br][:br]
   "You should now be ready to start solving problems.  Happy coding!"])