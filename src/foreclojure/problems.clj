(ns foreclojure.problems
  (:use (foreclojure utils config
                     [social :only [tweet-link gist!]]
                     [feeds :only [create-feed]]
                     [users :only [golfer?]])
        (clojail [core :exclude [safe-read]] testers)
        somnium.congomongo
        (hiccup form-helpers page-helpers core)
        (amalloy.utils [debug :only [?]]
                       [reorder :only [reorder]])
        [amalloy.utils :only [defcomp]]
        compojure.core)
  (:require [sandbar.stateful-session :as session]
            [clojure.string :as s]))

(def total-solved (agent 0))

(defn get-problem [x]
  (from-mongo
   (fetch-one :problems :where {:_id x})))

(defn get-problem-list 
  ([] (get-problem-list {:approved true}))
  ([criteria] 
    (from-mongo
    (fetch :problems
           :only [:_id :title :tags :times-solved :user]
           :where criteria
           :sort {:_id 1}))))

(defn get-next-id []
  (from-mongo
    (inc (count (fetch :problems)))))

(defn next-unsolved-problem [solved-problems]
  (when-let [unsolved (->> (get-problem-list)
                           (remove (comp (set solved-problems) :_id))
                           (seq))]
    (apply min-key :_id unsolved)))

(defn next-problem-link [completed-problem-id]
  (when-let [{:keys [solved]} (get-user (session/session-get :user))]
    (if-let [{:keys [_id title]} (next-unsolved-problem solved)]
      (str "Now try <a href='/problem/" _id "'>" title "</a>!")
      "You've solved them all! Come back later for more!")))

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

(defcomp mongo-key-from-number
  "Turn an integer into a key suitable for fetching from mongodb."
  [id]
  keyword str int)

(defn code-length [code]
  (count (remove #(Character/isWhitespace %)
                 code)))

(defn record-golf-score! [user-name problem-id score]
  (let [user-score-key (keyword (str "scores." problem-id))
        problem-score-key (keyword (str "scores." score))
        [problem-scores-key user-subkey] (map mongo-key-from-number
                                              [score problem-id])]
    (when-let [{:keys [_id scores] :as user}
               (from-mongo
                (fetch-one :users
                           :where {:user user-name}))]
      (let [old-score-real (get scores user-subkey)
            old-score-test (or old-score-real 1e6)
            old-score-key (keyword (str "scores." old-score-real))]
        (when (golfer? user)
          (session/session-put! :golf-chart
                                {:id problem-id
                                 :score score
                                 :best old-score-real}))
        (when (< score old-score-test)
          (update! :problems
                   {:_id problem-id,
                    old-score-key {:$gt 0}}
                   {:$inc {old-score-key -1}})
          (update! :problems
                   {:_id problem-id}
                   {:$inc {problem-score-key 1}})
          (update! :users
                   {:_id _id}
                   {:$set {user-score-key score}}))))))

(defn mark-completed [problem code & [user]]
  (let [user (or user (session/session-get :user))
        {:keys [_id approved]} problem
        gist-link (html [:div.share
                         [:a.novisited {:href "/share/code"} "Share"]
                         " this solution with your friends!"])
        message
        (cond
         (not approved) (str "You've solved the unapproved problem. Now you can approve it!")
         user (do
                (when (not-any? #{_id} (get-solved user))
                  (update! :users {:user user} {:$addToSet {:solved _id}})
                  (update! :problems {:_id _id} {:$inc {:times-solved 1}})
                  (send total-solved inc))
                (record-golf-score! user _id (code-length code))
                (str "Congratulations, you've solved the problem!"
                     "<br />" (next-problem-link _id)))
         :else (str "You've solved the problem! If you "
                    (login-link "log in") " we can track your progress."))]
    (session/session-put! :code [_id code])
    (flash-msg (str message " " gist-link) (str "/problem/" _id))))

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

(defn run-code [id raw-code]
  (let [code (.trim raw-code)
        {:keys [tests restricted] :as problem} (get-problem id)
        sb-tester (get-tester restricted)]
    (session/flash-put! :code code)
    (try
      (let [user-forms (s/join " " (map pr-str (read-string-safely code)))]
        (if (empty? user-forms)
          (flash-msg "Empty input is not allowed" *url*)
          (loop [[test & more] tests
                 i 0]
            (session/flash-put! :failing-test i)
            (if-not test
              (mark-completed problem code)
              (let [testcase (s/replace test "__" user-forms)]
                (if (sb sb-tester (first (read-string-safely testcase)))
                  (recur more (inc i))
                  (flash-msg "You failed the unit tests." *url*)))))))
      (catch Exception e
        (flash-msg (.getMessage e) *url*)))))

(defn render-test-cases [tests]
  [:table {:class "testcases"}
   (let [fail (session/flash-get :failing-test)]
     (for [[idx test] (map-indexed list tests)]
       [:tr
        [:td
         [:img {:src (cond
                      (or (nil? fail) (> idx fail)) "/images/bluelight.png"
                      (= idx fail) "/images/redlight.png"
                      :else "/images/greenlight.png")}]]
        [:td
         [:pre {:class "brush: clojure;gutter: false;toolbar: false;light: true"}
          test]]]))])

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

(def-page code-box [id]
  (let [{:keys [title tags description restricted tests approved user]}
        (get-problem (Integer. id))]
    [:div
     [:span#prob-title
      (when-not approved
        "Unapproved: ")
      title]
     [:hr]
     [:div#tags "Tags: "
      (s/join " " tags)]
     [:br]
     (when-not approved
       [:div#submitter "Submitted by: " user])
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
       [:span#message-text (session/flash-get :message)]]
      (render-golf-score)]
     (form-to [:post *url*]
              [:br]
              [:br]
       [:p#instruct "Code which fills in the blank: "]
       (text-area {:id "code-box"
                   :spellcheck "false"}
                  :code (session/flash-get :code))
       [:div#golfgraph
        (render-golf-chart)]
       (hidden-field :id id)
       [:br]
       [:button.large {:id "run-button" :type "submit"} "Run"]
       (when-not approved
         [:span [:button.large {:id "reject-button"} "Reject"]
                [:button.large {:id "approve-button"} "Approve"]]))
     ]))

(def-page problem-page []
  [:div.message (session/flash-get :message)]
  [:div#problems-error.error (session/flash-get :error)]
  (link-to "/problems/rss" [:div {:class "rss"}])
  [:table#problem-table.my-table
   [:thead
    [:tr
     [:th "Title"]
     [:th "Tags"]
     [:th "Times Solved"]
     [:th "Solved?"]]]
   (let [solved (get-solved (session/session-get :user))
         problems (get-problem-list)]
     (map-indexed
      (fn [x {:keys [title times-solved tags], id :_id}]
        [:tr (row-class x)
         [:td.titlelink
          [:a {:href (str "/problem/" id)}
           title]]
         [:td.centered
          (s/join " " (map #(str "<span class='tag'>" % "</span>")
                           tags))]
         [:td.centered (int times-solved)]
         [:td.centered
          [:img {:src (if (contains? solved id)
                        "/images/checkmark.png"
                        "/images/empty-sq.png")}]]])
      problems))])

(def-page unapproved-problem-page []
  [:div.message (session/flash-get :message)]
  [:div#problems-error.error (session/flash-get :error)]
  [:table#unapproved-problems.my-table
   [:thead
    [:tr
     [:th "Title"]
     [:th "Tags"]
     [:th "Submitted By"]]]
   (let [problems (get-problem-list {:approved false})]
     (map-indexed
      (fn [x {:keys [title tags user], id :_id}]
        [:tr (row-class x)
         [:td.titlelink
          [:a {:href (str "/problem/" id)}
           title]]
         [:td.centered
          (s/join " " (map #(str "<span class='tag'>" % "</span>")
                           tags))]
         [:td.centered user]])
      problems))])

(defn unapproved-problems []
  (let [user (session/session-get :user)]
    (if (approver? user)
      (unapproved-problem-page)
      (flash-error "You cannot access this page" "/problems"))))


(def-page problem-submission-page []
  [:div.instructions
    [:p "Thanks for choosing to submit a problem. Please make sure that you own the rights to the code you are submitting and that you wouldn't
        mind having us use the code as a 4clojure problem."]]
  (form-to {:id "problem-submission"} [:post "/problems/submit"]
           (label :title "Problem Title")
           (text-field :title)
           (label :tags "Tags (space separated)")
           (text-field :tags)
           (label :description "Problem Description")
           (text-area {:id "problem-description"} :description)
           [:br]
           (label :code-box "Problem test cases. Use two underscores (__) for user input. Multiple tests ought to be on one line each.")
           (text-area {:id "code-box" :spellcheck "false"}
                         :code (session/flash-get :code))
           [:p
             [:button.large {:id "run-button" :type "submit"} "Submit"]])
   )

(defn create-problem
  "create a user submitted problem"
  [title tags description code]
  (let [user (session/session-get :user)]
    (if (can-submit? user)
      (do
        (mongo! :db :mydb)
        (insert! :problems
                 {:_id (get-next-id)
                  :title title
                  :times-solved 0
                  :description description
                  :tags (s/split tags #"\s+")
                  :tests (s/split-lines code)
                  :user user
                  :approved false})
        (flash-msg "Thank you for submitting a problem! Be sure to check back to see it posted." "/problems"))
      (flash-error "You are not authorized to submit a problem." "/problems"))))

(defn approve-problem [id]
  "take a user submitted problem and approve it"
  (if (approver? (session/session-get :user))
    (do
      (update! :problems
        {:_id id}
        {:$set {:approved true}})
      (flash-msg (str "Problem " id " has been approved!")
                 (str "/problem/" id)))
    (flash-error "You don't have access to this page" "/problems")))

(defn reject-problem [id]
  "reject a user submitted problem by deleting it from the database"
  (if (approver? (session/session-get :user))
    (do
      (destroy! :problems
        {:_id id})
      ;; TODO: email submitting user
      (flash-msg (str "Problem " id " was rejected and deleted.") "/problems"))
    (flash-error "You do not have permission to access this page" "/problems")))

(defroutes problems-routes
  (GET "/problems" [] (problem-page))
  (GET "/problem/:id" [id] (code-box id))
  (GET "/problems/submit" [] (problem-submission-page))
  (POST "/problems/submit" [title tags description code]
    (create-problem title tags description code))
  (GET "/problems/unapproved" [] (unapproved-problems))
  (POST "/problem/approve" [id]
    (approve-problem (Integer. id)))
  (POST "/problem/reject" [id]
    (reject-problem (Integer. id)))
  (POST "/problem/:id" [id code]
    (run-code (Integer. id) code))
  (GET "/problems/rss" [] (create-feed
                           "4Clojure: Recent Problems"
                           "http://4clojure.com/problems"
                           "Recent problems at 4Clojure.com"
                           "http://4clojure.com/problems/rss"
                           (problem-feed 20))))
