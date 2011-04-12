(ns foreclojure.problems
  (:use [foreclojure.utils]
        [clojail core testers]
	[somnium.congomongo]
        [clojure.contrib.string :only [replace-str]]
        [hiccup form-helpers])
  (:require [sandbar.stateful-session :as session]))

(defn get-solved [user]
  (into #{}
        (:solved (fetch-one :users
                            :where {:user user}
                            :only [:solved]))))

(defn get-problem [x]
  (fetch-one :problems :where {:_id x}))

(defn get-problem-list []
  (fetch :problems
         :only [:_id :title :tags :times-solved]
         :sort {:id 1}))

(defn mark-completed [id]
  (if-let [user (session/session-get :user)]
    (do
      (when (not-any? #(= % id) (get-solved user))
                      
        (update! :users {:user user} {:$push {:solved id}})
        (update! :problems {:_id id} {:$inc {:times-solved 1}}))
      (flash-msg "Congratulations, you've solved the problem!" "/problems")) 
    (flash-msg "You've solved the problem! If you log in we can track your progress." "/problems")))

(defn get-tester [restricted]
  (reduce #(conj %1 (symbol %2)) secure-tester restricted))

(def sb (sandbox*))

(defn run-code [id code]
  (let [p (get-problem id)
        tests (concat (:tests p) (:secret-tests p))
        func-name (:function-name p)
        sb-tester (get-tester (:restricted p))]
    (try
      (loop [t tests]
        (if (empty? t)
          (mark-completed id)
          (let [testcase (replace-str "__" code (first t))]
            (if (sb sb-tester (read-string testcase))
              (recur (rest t))
              (do
                (session/flash-put! :code code)
                (flash-error "You failed the unit tests."
                           (str "/problem/" id)))))))
      (catch Exception e
        (do
          (session/flash-put! :code code)
          (flash-error (.getMessage e) (str "/problem/" id)))))))


(def-page code-box [id]
  (let [problem (dbg (get-problem (Integer. id)))]
    [:div
     [:span {:id "prob-title"} (problem :title)]
     [:hr]
     [:div {:id "prob-desc"}
      (problem :description)[:br]
      [:div {:id "testcases"}
       (map #(vec [:li {:class "testcase"} %]) (problem :tests))]
      (if-let [restricted (problem :restricted)]
        [:div {:id "restrictions"}
         [:u "Special Restrictions"] [:br]
         (map #(vec [:li %]) restricted)])]
     [:div
      [:b "Enter your code:" [:br]
       [:span {:class "error"} (session/flash-get :error)]]]
     (form-to [:post "/run-code"] 
              (text-area {:id "code-box"
                          :spellcheck "false"}
                         :code (session/flash-get :code))
              (hidden-field :id id)
              (submit-button {:type "image" :src "/run.png"} "Run"))]))

(def-page problem-page []
  [:div {:class "congrats"} (session/flash-get :message)]
  [:table {:class "my-table" :width "100%"}
   [:th "Title"]
   [:th "Tags"]
   [:th "Count"]
   [:th "Solved?"]
   (let [solved (get-solved (session/session-get :user))]
     (map-indexed
      (fn [x p]
        (vec [:tr (row-class x)
              [:td {:class "title-link"}
               [:a {:href (str "/problem/" (p :_id))}
                (p :title)]]
              [:td {:class "centered"}
               (map #(str % " ") (p :tags))]
              [:td {:class "centered"} (p :times-solved)]
              [:td {:class "centered"}
               (if (contains? solved (p :_id))
                 [:img {:src "/checkmark.png"}]
                 [:img {:src "/empty-sq.png"}])]]))
      (get-problem-list)))])