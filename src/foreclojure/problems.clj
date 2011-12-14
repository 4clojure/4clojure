(ns foreclojure.problems
  (:require [foreclojure.users        :as      users]
            [sandbar.stateful-session :as      session]
            [clojure.string           :as      s]
            [ring.util.response       :as      response]
            [cheshire.core            :as      json])
  (:import  [org.apache.commons.mail  EmailException])
  (:use     [foreclojure.utils        :only    [from-mongo get-user get-solved login-link flash-msg flash-error row-class approver? can-submit? send-email image-builder if-user with-user as-int maybe-update escape-html user-id]]
            [foreclojure.ring-utils   :only    [*url*]]
            [foreclojure.template     :only    [def-page content-page]]
            [foreclojure.social       :only    [tweet-link gist!]]
            [foreclojure.feeds        :only    [create-feed]]
            [foreclojure.users        :only    [golfer? get-user-id disable-codebox?]]
            [foreclojure.solutions    :only    [save-solution get-solution]]
            [clojail.core             :exclude [safe-read]]
            [clojail.testers          :only    [secure-tester]]
            [somnium.congomongo       :only    [update! fetch-one fetch fetch-and-modify destroy!]]
            [hiccup.form-helpers      :only    [form-to text-area hidden-field label text-field drop-down]]
            [hiccup.page-helpers      :only    [link-to]]
            [hiccup.core              :only    [html]]
            [useful.debug             :only    [?]]
            [compojure.core           :only    [defroutes GET POST]]))

(def solved-stats (agent {:total 0}))

(defn get-problem [x]
  (from-mongo
   (fetch-one :problems :where {:_id x})))

(defn get-problem-list
  ([] (get-problem-list {:approved true}))
  ([criteria]
     (from-mongo
      (fetch :problems
             :only [:_id :title :difficulty :tags :user]
             :where criteria
             :sort {:_id 1}))))

(defn next-unsolved-problem [solved-problems just-solved-id]
  (when-let [unsolved (seq
                       (from-mongo
                        (fetch :problems
                               :only [:_id :title]
                               :where {:_id {:$nin solved-problems}, :approved true}
                               :sort {:_id 1})))]
    (let [[skipped not-yet-tried] (split-with #(< (:_id %) just-solved-id)
                                              unsolved)]
      (filter identity [(rand-nth (or (seq skipped)
                                      [nil])) ; rand-nth barfs on empty seq
                        (first not-yet-tried)]))))

(letfn [(problem-link [{id :_id title :title}]
          (str "<a href='/problem/" id "#prob-title'>" title "</a>"))]
  (defn suggest-problems
    ([] "You've solved them all! Come back later for more!")
    ([problem]
       (str "Now try " (problem-link problem) "!"))
    ([skipped not-tried]
      (str "Now you can move on to " (problem-link not-tried)
           ", or go back and try " (problem-link skipped) " again!"))))

(defn next-problem-link [completed-problem-id]
  (when-let [{:keys [solved]} (get-user (session/session-get :user))]
    (apply suggest-problems
           (next-unsolved-problem solved completed-problem-id))))

(defn get-recent-problems [n]
  (map get-problem (map :_id (take-last n (get-problem-list)))))

(defn problem-feed [n]
  (reduce (fn [feed v]
            (conj feed [:item
                        [:guid (str "http://4clojure.com/problem/" (:_id v))]
                        [:title (:title v)]
                        [:description (:description v)]]))
          ()
          (get-recent-problems n)))

(defn mongo-key-from-number
  "Turn an integer into a key suitable for fetching from mongodb."
  [id]
  (keyword (str (int id))))

(defn number-from-mongo-key
  "Turn a keyword like :4 into an integer"
  [k]
  (Integer. (name k)))

(defn trim-code [code]
  (when code (.trim code)))

(defn code-length [code]
  (count (remove #(or (Character/isWhitespace %)
                      (= % \,))
                 code)))

(defn record-golf-score! [user-id problem-id score]
  (when-let [{{old-score (keyword (str problem-id))} :scores
              user-id :_id :as user}
             (from-mongo
              (fetch-one :users
                         :where {:_id user-id}))]
    (when (golfer? user)
      (session/session-put! :golf-chart
                            {:id problem-id
                             :score score
                             :best old-score}))
    (when (or (not old-score)
              (> old-score score))
      (update! :users
               {:_id user-id}
               {:$set {(keyword (str "scores." problem-id)) score}})
      (send solved-stats (fn [scores]
                           (let [inc (fnil inc 0),
                                 dec (fn [x]
                                       (when (and x (> x 1))
                                         (dec x)))]
                             (maybe-update scores [problem-id]
                                           #(-> %
                                                (maybe-update [score] inc)
                                                (maybe-update [old-score] dec)))))))))

(defn store-completed-state! [username problem-id code]
  (let [user-id (user-id username)
        current-time (java.util.Date.)]
    (when (not-any? #{problem-id} (get-solved username))
      (update! :users {:_id user-id} {:$addToSet {:solved problem-id}
                                      :$set {:last-solved-date current-time}})
      (let [inc (fnil inc 0)]
        (send solved-stats
              #(-> %
                 (update-in [:total] inc)
                 (update-in [:solved-counts problem-id] inc)))))
    (record-golf-score! user-id problem-id (code-length code))
    (save-solution user-id problem-id code)))

(defn mark-completed [problem code & [user]]
  (let [user (or user (session/session-get :user))
        {:keys [_id approved]} problem
        gist-link (html [:span.share
                         [:a.novisited {:href "/share/code"} "share"]
                         " this solution on github and twitter!  "])
        message
        (cond
         (not approved) (str "You've solved the unapproved problem. Now you can approve it!")
         user (do
                (store-completed-state! user _id code)
                (str "Congratulations, you've solved the problem!  See the "
                     "<a href='/problem/solutions/" _id "'>solutions</a>"
                     " that the users you follow have submitted, or "
                     gist-link
                     (next-problem-link _id)))
         :else (str "You've solved the problem; "
                    gist-link
                    "You need to " (login-link "log in" (str "/problem/" _id)) " in order to save your solutions and track progress."))]
    (session/session-put! :code [_id code])
    {:message message, :error "",  :url (str "/problem/" _id)}))

(def restricted-list '[use require in-ns future agent send send-off pmap pcalls])

(defn get-tester [restricted]
  (into secure-tester (concat restricted-list (map symbol restricted))))

(def sb (sandbox*))

(defn read-string-safely [s]
  (binding [*read-eval* false]
    (with-in-str s
      (let [end (Object.)]
        (doall (take-while (complement #{end})
                           (repeatedly #(read *in* false end))))))))

(defn run-code
  "Run the specified code-string against the test cases for the problem with the
specified id.

Return a map, {:message, :error, :url, :num-tests-passed}."
  [id code]
  (try
    (let [{:keys [tests restricted] :as problem} (get-problem id)
          sb-tester (get-tester restricted)
          user-forms (s/join " " (map pr-str (read-string-safely code)))
          devnull (java.io.StringWriter.) ;; TODO consider Apache Commons IO
          results (if (empty? user-forms)
                    ["Empty input is not allowed."]
                    (for [test tests]
                      (try
                        (when-not (sb (->> user-forms
                                           (s/replace test "__")
                                           read-string-safely
                                           first)
                                      sb-tester
                                      {#'*out* devnull
                                       #'*err* devnull})
                          "You failed the unit tests")
                        (catch Throwable t (.getMessage t)))))
          [passed [fail-msg]] (split-with nil? results)]
      (assoc (if fail-msg
               {:message "", :error fail-msg, :url *url*}
               (mark-completed problem code))
        :num-tests-passed (count passed)))
    (catch Throwable t {:message "" :error (.getMessage t), :url *url*
                        :num-tests-passed 0})))

(defn static-run-code [id code]
  (session/flash-put! :code code)
  (let [{:keys [message error url num-tests-passed]}
        (binding [*url* (str *url* "#prob-desc")]
          (run-code id code))]
    (session/flash-put! :failing-test num-tests-passed)
    (flash-msg url message)
    (flash-error url error)))

(let [light-img (image-builder {:red   ["red"   "test failed"]
                                :green ["green" "test passed"]
                                :blue  ["blue"  "test not run"]}
                               :src #(str "images/" % "light.png"))]
  (defn render-test-cases [tests]
    [:table {:class "testcases"}
     (let [fail (session/flash-get :failing-test)]
       (for [[idx test] (map-indexed list tests)]
         [:tr
          [:td
           (light-img (cond
                       (or (nil? fail) (> idx fail)) :blue
                       (= idx fail)                  :red
                       :else                         :green))]
          [:td
           [:pre {:class "brush: clojure;gutter: false;toolbar: false;light: true"}
            test]]]))]))

(defn render-golf-chart []
  (let [{:keys [id best score] :as settings}
        (session/session-get :golf-chart)

        url (str "/leagues/golf/" id "?best=" best "&curr=" score)]
    (session/session-delete-key! :golf-chart)
    (when settings
      [:img {:src url}])))

(defn render-golf-score []
  (let [{:keys [id best score] :as settings}
        (session/session-get :golf-chart)]
    (when settings
      [:div#golf-scores
       [:p#golfheader (str "Code Golf Score: " score)]
       [:a.graph-class {:href "#"
                        :onclick "return false"}
        [:span#graph-link "View Chart"]]])))

(defn rest-run-code [id raw-code]
  (let [{:keys [message error url num-tests-passed]} (run-code id raw-code)]
    (json/generate-string {:failingTest num-tests-passed
                           :message message
                           :error error
                           :golfScore (html (render-golf-score))
                           :golfChart (html (render-golf-chart))})))

(defn wants-no-javascript-codebox? []
  (if-user [{:keys [user] :as user-obj}]
    (disable-codebox? user-obj)))

(def-page code-box [id]
  (let [{:keys [_id title difficulty tags description
                restricted tests approved user]}
        (get-problem (Integer. id)),
        title (str (when-not approved
                     "Unapproved: ")
                   title)]

    {:title (str _id ". " title)
     :content
     [:div
      [:div#prob-title title]
      (if-user [{:keys [solved]}]
        (if (some #{(Integer. id)} solved)
          (link-to (str "/problem/solutions/" id)
                   [:button#solutions-link {:type "submit"} "Solutions"])
          [:div {:style "clear: right; margin-bottom: 15px;"} "&nbsp;"])
        [:div {:style "clear: right; margin-bottom: 15px;"} "&nbsp;"])
      [:hr]
      [:table#tags
       [:tr [:td "Difficulty:"] [:td (or difficulty "N/A")]]
       [:tr [:td "Topics:"]     [:td (s/join " " tags)]]]
      [:br]
      (when-not approved
        [:div#submitter "Submitted by: "
         (users/mailto user)])
      [:br]
      [:div#prob-desc
       description[:br]
       (render-test-cases tests)
       (when restricted
         [:div#restrictions
          [:u "Special Restrictions"] [:br]
          (map (partial vector :li) restricted)])]
      [:div
       [:div.message
        [:span#message-text (session/flash-get :message)]
        [:span#error-message-text.error (session/flash-get :error)]]
       [:div#golfscore
        (render-golf-score)]]
      (form-to {:id "run-code"} [:post *url*]
        [:br]
        [:br]
        [:p#instruct "Code which fills in the blank: "]
        (when (wants-no-javascript-codebox?) [:span#disable-javascript-codebox])
        (text-area {:id "code-box"
                    :name "code"
                    :spellcheck "false"}
                   :code (escape-html
                          (or (session/flash-get :code)
                              (-> (session/session-get :user)
                                  (get-user-id)
                                  (get-solution ,,, _id)))))
        [:div#golfgraph
         (render-golf-chart)]
        (hidden-field :id id)
        [:br]
        [:button.large {:id "run-button" :type "submit"} "Run"]
        (when-not approved
          [:span [:button.large {:id "reject-button"} "Reject"]
           [:button.large {:id "edit-button"} "Edit"]
           [:button.large {:id "approve-button"} "Approve"]]))]}))

(defn problem-page [id]
  (let [error #(flash-error "/problems" %)
        user (delay (session/session-get :user))]
    (if-let [{:keys [approved]} (get-problem id)]
      (cond (or approved (approver? (force user))) (code-box id)
            (force user) (error "You cannot access this page")
            :else (error (str "You must " (login-link)
                              " to view unapproved problems")))
      (error "No such problem!"))))

(def-page show-solutions-page [problem-id]
  {:title "4Clojure - Problem Solutions"
   :content
   (list
    [:div.message (session/flash-get :message)]
    [:div#problems-error.error (session/flash-get :error)]
    [:h3 {:style "margin-top: -20px;"} "Solutions:"]
    (with-user [{:keys [_id following]}]
      (list
       (let [user-code (get-solution :public _id problem-id)]
         [:pre.solution-code.solution-user-code (escape-html user-code)])
       (if (empty? following)
         [:p "You can only see solutions of users whom you follow.  Click on any name from the " (link-to "/users" "users") " listing page to see their profile, and click follow from there."]
         (if (some (complement nil?) (map #(get-solution :public % problem-id) following))
           (interpose [:hr.solution]
                      (for [f-user-id following
                            :let [f-user (:user (from-mongo
                                                 (fetch-one :users
                                                            :where {:_id f-user-id}
                                                            :only [:user])))
                                  f-code (get-solution :public
                                                       f-user-id problem-id)]
                            :when f-code]
                        [:div.follower-solution
                         [:div.solution-username (str f-user "'s solution:")]
                         [:pre.solution-code (escape-html f-code)]]))
           [:p "None of the users you follow have solved this problem yet!"])))))})

(defn show-solutions [id]
  (let [problem-id (Integer. id)
        user (session/session-get :user)]
    (if-user [{:keys [solved]}]
      (if (some #{problem-id} solved)
        (show-solutions-page problem-id)
        (flash-error (str "/problem/" problem-id)
          "You must solve this problem before you can see others' solutions!"))
      (do
        (session/session-put! :login-to *url*)
        (flash-error "/login" "You must log in to see solutions!")))))

(let [checkbox-img (image-builder {true ["images/checkmark.png" "completed"]
                                   false ["images/empty-sq.png" "incomplete"]})]
  (def-page problem-list-page []
    {:title "4clojure - Problem Listing"
     :content
     (list
      [:div.message (session/flash-get :message)]
      [:div#problems-error.error (session/flash-get :error)]
      (link-to "/problems/rss" [:div {:class "rss"}])
      [:table#problem-table.my-table
       [:thead
        [:tr
         [:th "Title"]
         [:th "Difficulty"]
         [:th "Topics"]
         [:th "Submitted By"]
         [:th "Times Solved"]
         [:th "Solved?"]]]
       (let [solved (get-solved (session/session-get :user))
             problems (get-problem-list)]
         (map-indexed
          (fn [x {:keys [title difficulty tags user], id :_id}]
            [:tr (row-class x)
             [:td.titlelink
              [:a {:href (str "/problem/" id)}
               title]]
             [:td.centered difficulty]
             [:td.centered
              (s/join " " (map #(str "<span class='tag'>" % "</span>")
                               tags))]
             [:td.centered user]
             [:td.centered (get-in @solved-stats [:solved-counts id] 0)]
             [:td.centered (checkbox-img (contains? solved id))]])
          problems))])}))

(defn generate-unapproved-problems-list []
  (let [problems (get-problem-list {:approved false})]
    (list
     [:table#unapproved-problems.my-table
      [:thead
       [:tr
        [:th "Title"]
        [:th "Difficulty"]
        [:th "Topics"]
        [:th "Submitted By"]]]
      (map-indexed
       (fn [x {:keys [title difficulty tags user], id :_id}]
         [:tr (row-class x)
          [:td.titlelink
           [:a {:href (str "/problem/" id)}
            title]]
          [:td.centered difficulty]
          [:td.centered
           (s/join " " (map #(str "<span class='tag'>" % "</span>")
                            tags))]
          [:td.centered user]])
       problems)])))

(def-page unapproved-problem-list-page []
  {:title "Unapproved problems"
   :content
   (content-page
    {:main (generate-unapproved-problems-list)})})

(defn access-unapproved-problem-list-page []
  (let [user (session/session-get :user)]
    (if (approver? user)
      (unapproved-problem-list-page)
      (flash-error "/problems" "You cannot access this page"))))

(def-page problem-submission-page []
  {:title "Submit a problem"
   :content
   (list
    [:div.instructions
     [:p "Thanks for choosing to submit a problem. Please make sure that you own the rights to the code you are submitting and that you wouldn't mind having us use the code as a 4clojure problem.  Once you've submitted your problem, it won't appear on the site until someone from the 4clojure team has had a chance to review it."]]
    (form-to {:id "problem-submission"} [:post "/problems/submit"]
      (hidden-field :author (session/flash-get :author))
      (hidden-field :prob-id (session/flash-get :prob-id))
      (label :title "Problem Title")
      (text-field :title  (session/flash-get :title))
      (label :diffulty "Difficulty")
      (drop-down :difficulty ["Elementary" "Easy" "Medium" "Hard"] (session/flash-get :difficulty))
      (label :tags "Topics (space separated)")
      (text-field :tags  (session/flash-get :tags))
      (label :restricted "Restricted Functions (space separated)")
      (text-field :restricted  (session/flash-get :restricted))
      (label :description "Problem Description")
      (text-area {:id "problem-description"} :description  (session/flash-get :description))
      [:br]
      (label :code-box "Problem test cases. Use two underscores (__) for user input. Individual tests can span multiple lines, but each test should be separated by a totally blank line.")
      (text-area {:id "code-box" :spellcheck "false"}
                 :code (session/flash-get :tests))
      [:p
       [:button.large {:id "submission-button" :type "submit"} "Submit"]]))})

(defn create-problem
  "create a user submitted problem"
  [title difficulty tags restricted description code id author]
  (let [user (session/session-get :user)]
    (if (or (approver? user)
            (and (can-submit? user)
                 (not id)))
      (let [id (or id
                   (:seq (fetch-and-modify
                          :seqs
                          {:_id "problems"}
                          {:$inc {:seq 1}})))
            edit-url (str "https://4clojure.com/problem/"
                          id)
            existing-problem (fetch-one :problems
                                        :where {:_id id}
                                        :only [:approved :times-solved])
            approved (true? (:approved existing-problem))]

        (when-not existing-problem
          (try
            (send-email
             {:from "team@4clojure.com"
              :to ["team@4clojure.com"]
              :reply-to [(users/email-address user)]
              :subject (str "User submission: " title)
              :html (html [:h3 (link-to edit-url title)]
                          [:div description])
              :text (str title ": " edit-url "\n" description)})
            ;; TODO: dump this in a proper log
            (catch EmailException e (println (str "email failed to send on newly submitted problem #" id)))))

        (update! :problems
                 {:_id id}
                 {:$set
                  {:title title
                   :difficulty difficulty
                   :description description
                   :tags (re-seq #"\S+" tags)
                   :restricted (re-seq #"\S+" restricted)
                   :tests (s/split code #"\r\n\r\n")
                   :user (if (empty? author) user author)
                   :approved approved}})
        (flash-msg "/problems"
          "Thank you for submitting a problem! Be sure to check back to see it posted."))
      (flash-error "/problems" "You are not authorized to submit a problem."))))

(defn edit-problem [id]
  (if (approver? (session/session-get :user))
    (let [{:keys [title user difficulty tags restricted description tests]} (get-problem id)]
      (doseq [[k v] {:prob-id id
                     :author user
                     :title title
                     :difficulty difficulty
                     :tags (s/join " " tags)
                     :restricted (s/join " " restricted)
                     :description description
                     :tests (s/join "\r\n\r\n" tests)}]
        (session/flash-put! k v))
      (response/redirect "/problems/submit"))
  (flash-error "/problems" "You don't have access to this page")))

(defn approve-problem [id]
  "take a user submitted problem and approve it"
  (if (approver? (session/session-get :user))
    (let [{:keys [title user]} (from-mongo
                                (fetch-one :problems
                                           :where {:_id id}
                                           :only [:title :user]))
          url (str "/problem/" id)]
      (update! :problems
               {:_id id}
               {:$set {:approved true}})
      (try
        (send-email
         {:from "team@4clojure.com"
          :to [(users/email-address user)]
          :subject (format "Problem #%d: submission accepted" id)
          :html (html (link-to (str "http://www.4clojure.com/" url) title))
          :text (str title ": " url)})
        ;; TODO: dump this in a proper log
        (catch EmailException e (println (str "email failed to send on approved problem #" id))))
      (flash-msg url (str "Problem " id " has been approved!")))
    (flash-error "/problems" "You don't have access to this page")))

(defn reject-problem [id reason]
  "reject a user submitted problem by deleting it from the database"
  (if (approver? (session/session-get :user))
    (let [{:keys [user title description tags tests]} (get-problem id)
          email (:email (get-user user))]
      (destroy! :problems
                {:_id id})
      (send-email
       {:from "team@4clojure.com"
        :to [email]
        :subject "Problem rejected"
        :text
        (str "A problem you've submitted has been rejected, but don't get discouraged!\n"
             "Check out the reason below, and try again.\n\n"
             "Title: " title "\n"
             "Tags: " tags "\n"
             "Description: " description "\n"
             "Tests: " tests "\n"
             "Rejection Reason: " reason)})
      (flash-msg "/problems" (str "Problem " id " was rejected and deleted.")))
    (flash-error "/problems" "You do not have permission to access this page")))

(defn total-solved-count []
  (html (:total @solved-stats)))

(defroutes problems-routes
  (GET "/problems" [] (problem-list-page))
  (GET "/problems/solved" [] (total-solved-count))
  (GET "/problem/:id" [id]
    (if-let [id-int (as-int id)]
      (problem-page id-int)
      (flash-error "/problems"
        (format "'%s' is not a valid problem number." id))))
  (GET "/problems/submit" [] (problem-submission-page))
  (POST "/problems/submit" [prob-id author title difficulty tags restricted description code]
    (create-problem title difficulty tags restricted description code (when (not= "" prob-id) (Integer. prob-id)) author))
  (GET "/problems/unapproved" [] (access-unapproved-problem-list-page))
  (GET "/problem/:id/edit" [id]
    (edit-problem (Integer. id)))
  (POST "/problem/edit" [id]
    (edit-problem (Integer. id)))
  (POST "/problem/approve" [id]
    (approve-problem (Integer. id)))
  (POST "/problem/reject" [id]
    (reject-problem (Integer. id) "We didn't like your problem."))
  (GET "/problem/solutions/:id" [id]
    (show-solutions id))
  (POST "/problem/:id" [id code]
    (static-run-code (Integer. id) (trim-code code)))
  (POST "/rest/problem/:id" [id code]
    {:headers {"Content-Type" "application/json"}}
    (rest-run-code (Integer. id) (trim-code code)))
  (GET "/problems/rss" [] (create-feed
                           "4Clojure: Recent Problems"
                           "http://4clojure.com/problems"
                           "Recent problems at 4Clojure.com"
                           "http://4clojure.com/problems/rss"
                           (problem-feed 20))))
