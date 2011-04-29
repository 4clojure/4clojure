(ns foreclojure.problems
  (:use foreclojure.utils
        [foreclojure.social :only [tweet-link gist!]]
        [foreclojure.feeds :only [create-feed]]
        [clojail core testers]
        somnium.congomongo
        (hiccup form-helpers page-helpers core)
        [amalloy.utils.debug :only [?]]
        compojure.core)
  (:require [sandbar.stateful-session :as session]
            [clojure.string :as s]))

(def total-solved (agent 0))

(defn get-solved [user]
  (set
   (:solved (from-mongo
             (fetch-one :users
                        :where {:user user}
                        :only [:solved])))))

(defn get-problem [x]
  (from-mongo
   (fetch-one :problems :where {:_id x})))

(defn get-problem-list []
  (from-mongo
   (fetch :problems
          :only [:_id :title :tags :times-solved]
          :sort {:_id 1})))

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

(defn mark-completed [id code & [user]]
  (let [user (or user (session/session-get :user))
        gist-link (html [:div.share
                         [:a.novisited {:href "/share/code"} "Share"]
                         " this solution with your friends!"])

        message
        (if user
          (do
            (when (not-any? #{id} (get-solved user))
              (update! :users {:user user} {:$addToSet {:solved id}})
              (update! :problems {:_id id} {:$inc {:times-solved 1}})
              (send total-solved inc))
            "Congratulations, you've solved the problem!")
          "You've solved the problem! If you log in we can track your progress.")]
    (session/session-put! :code [id code])
    (flash-msg (str message " " gist-link) "/problems")))

(def restricted-list '[use require in-ns future agent send send-off pmap pcalls])

(defn get-tester [restricted]
  (into secure-tester (concat restricted-list (map symbol restricted))))

(def sb (sandbox*))

(defn run-code [id raw-code]
  (let [code (.trim raw-code)
        {:keys [tests restricted]} (get-problem id)
        sb-tester (get-tester restricted)]
    (if (empty? code)
      (do
        (session/flash-put! :code code)
        (flash-error "Empty input is not allowed"
                     (str "/problem/" id)))
      (try
        (loop [[test & more] tests]
          (if-not test
            (mark-completed id code)
            (let [testcase (s/replace test "__" (str code))]
              (if (sb sb-tester (safe-read testcase))
                (recur more)
                (do
                  (session/flash-put! :code code)
                  (flash-error "You failed the unit tests."
                               (str "/problem/" id)))))))
        (catch Exception e
          (do
            (session/flash-put! :code code)
            (flash-error (.getMessage e) (str "/problem/" id))))))))


(def-page code-box [id]
  (let [problem (get-problem (Integer. id))]
    [:div
     [:span {:id "prob-title"} (problem :title)]
     [:hr]
     [:div {:id "tags"} "Tags: "
      (s/join " " (problem :tags))]
     [:br]
     [:div {:id "prob-desc"}
      (problem :description)[:br]
      [:div {:class "testcases"}
       [:pre {:class "brush: clojure;gutter: false;toolbar: false;light: true"}
        (for [test (:tests problem)]
          (str test "\n"))]]
      (if-let [restricted (problem :restricted)]
        [:div {:id "restrictions"}
         [:u "Special Restrictions"] [:br]
         (map (partial vector :li) restricted)])]
     [:div
      [:b "Code which fills in the blank:" [:br]
       [:span {:class "error"} (session/flash-get :error)]]]
     (form-to [:post "/run-code"]
             (text-area {:id "code-box"
                          :spellcheck "false"}
                         :code (session/flash-get :code))
              (hidden-field :id id)
              [:br]
              [:button.large {:id "run-button" :type "submit"} "Run"])]))

(def-page problem-page []
  [:div.congrats (session/flash-get :message)]
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

(defroutes problems-routes
  (GET "/problems" [] (problem-page))
  (GET "/problem/:id" [id] (code-box id))
  (GET "/problems/rss" [] (create-feed
                           "4Clojure: Recent Problems"
                           "http://4clojure.com/problems"
                           "Recent problems at 4Clojure.com"
                           "http://4clojure.com/problems/rss"
                           (problem-feed 20)))
  (POST "/run-code" {{:strs [id code]} :form-params}
          (run-code (Integer. id) code)))
