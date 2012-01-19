(ns foreclojure.template
  (:require [sandbar.stateful-session  :as   session])
  (:use     [hiccup.core               :only [html]]
            [hiccup.page-helpers       :only [doctype javascript-tag link-to]]
            [foreclojure.config        :only [config repo-url]]
            [foreclojure.utils         :only [page-attributes rendering-info login-url approver? can-submit?]]
            [foreclojure.ring-utils    :only [static-url]]
            [foreclojure.version-utils :only [css js]]))

;; Global wrapping template
(defn html-doc [body]
  (let [attrs (rendering-info (page-attributes body))
        user (session/session-get :user)]
    (html
     (doctype :html5)
     [:html
      [:head
       [:title (:title attrs)]
       [:link {:rel "alternate" :type "application/atom+xml" :title "Atom" :href "/problems/rss"}]
       [:link {:rel "shortcut icon" :href (static-url "favicon2.ico")}]
       [:style {:type "text/css"}
        ".syntaxhighlighter { overflow-y: hidden !important; }"]
       (css "css/style.css" "css/demo_table.css" "css/shCore.css" "css/shThemeDefault.css")
       (css "css/impromptu.css")
       (js "vendor/script/jquery-1.5.2.min.js" "vendor/script/jquery.dataTables.min.js" "vendor/script/jquery.flipCounter.1.1.pack.js" "vendor/script/jquery.easing.1.3.js")
       (js "script/codebox.js" "script/foreclojure.js")
       (js "vendor/script/xregexp.js" "vendor/script/shCore.js" "vendor/script/shBrushClojure.js")
       (js "vendor/script/ace/ace.js" "vendor/script/ace/mode-clojure.js")
       (js "vendor/script/detectmobilebrowser.js")
       (js "vendor/script/jquery-impromptu.js")
       [:script {:type "text/javascript"} "SyntaxHighlighter.all()"]]
      [:body
       (when (:fork-banner attrs)
         [:div#github-banner [:a {:href repo-url
                                   :alt "Fork 4Clojure on Github!"}]])
        [:div#banner
          [:span#banner-text "( " [:a {:href "#" } "want-free-tickets?"]
            [:img#banner-logo {:src (static-url "images/clojure-west.png") :alt "Clojure West"}]
            " ) "]
          [:span#banner-info.hidden  "4Clojure is proud to be supporting Clojure/West, an upcoming conference targeted at Clojure developers of all levels of skill and experience.<br><br> Especially exciting is that we will be running a series of contests to get [discounts? free tickets?] to Clojure/West. The current contest goes from [date] to [date] - whoever submits the best new problem by the end of the contest will receive [the prize]."]
          (when-not (:fork-banner attrs)
            [:div#right-spacer])]
       [:div#top
        (link-to "/" [:img#logo {:src (static-url "images/4clj-logo-small.png")
                                  :alt "4clojure.com"}])]
       [:div#content
        [:div#menu
         (for [[link text & [tabbed]]
               [["/" "Main Page"]
                ["/problems" "Problem List"]
                ["/users" "Top Users"]
                ["/directions" "Help"]
                ["http://try-clojure.org" "REPL" true]
                ["http://clojuredocs.org" "Docs" true]]]
           [:a.menu (assoc (when tabbed {:target "_blank"})
                      :href link)
            text])
         [:div#user-info
          (if user
            [:div
             [:span#username (str "Logged in as " user)]
             [:a#logout {:href "/logout"} "Logout"]]
            [:div
             [:a#login {:href (login-url)} "Login"]
             [:a#register {:href "/register"} "Register"]])]]
        (when user
          [:div#lower-menu
           [:span
            (link-to "/settings" "Account Settings")]
           (when (:golfing-active config)
             [:span ; deserves its own page, but just make it discoverable for now
              (link-to "/league" "Leagues")])
           (when (approver? user)
             [:span
              (link-to "/problems/unapproved" "View Unapproved Problems")])
           (when (can-submit? user)
             [:span (link-to "/problems/submit" "Submit a Problem")])])
        [:div#content_body (:content attrs)]
        [:div#footer
         "The content on 4clojure.com is available under the EPL v 1.0 license."
         (let [email "team@4clojure.com"]
           [:span
            [:a#contact {:href (str "mailto:" email)} "Contact us"]
            (str " (" email ")")])]
        (javascript-tag
         "var _gaq = _gaq || [];
          _gaq.push(['_setAccount', 'UA-22844856-1']);
          _gaq.push(['_trackPageview']);

          (function() {
            var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
            ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
          })();"
         )]]])))

;; Content templates
(defn content-page [{:keys [heading heading-note sub-heading main]}]
  (let [flash-message (session/flash-get :message)
        flash-error   (session/flash-get :error)]
    (list
     (when heading       [:div#heading      heading])
     (when heading-note  [:div#heading-note heading-note])
     (when sub-heading   [:div#sub-heading  sub-heading])
     (when flash-message [:div.message
                          [:span#flash-text flash-message]])
     (when flash-error   [:div.message
                          [:span#error-text flash-error]])
     (when main          [:div#main         main]))))

(defmacro def-page [page-name [& args] & code]
  `(defn ~page-name [~@args]
     (html-doc (do ~@code))))