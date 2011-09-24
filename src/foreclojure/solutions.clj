(ns foreclojure.solutions
  (:require [clojure.string      :as   s])
  (:use     [somnium.congomongo  :only [fetch-one update!]]
            [useful.debug        :only [?]]))

(defn get-solution
  ([perm-level user-id problem-id]
     (when (or (= :private perm-level)
               (not (:hide-solutions (fetch-one :users
                                                :where {:_id user-id}
                                                :only [:hide-solutions]))))
       (get-solution user-id problem-id)))
  ([user-id problem-id]
     (or (:code (fetch-one :solutions
                           :where {:user user-id
                                   :problem problem-id}))
         (let [{:keys [scores solved]}
               (fetch-one :users
                          :where {:_id user-id}
                          :only [(keyword (str "scores." problem-id))
                                 :solved])]
           (cond (seq scores) (str "Scored " (first (vals scores))
                                   ", before 4clojure started storing solutions."),

                 (some #{problem-id} solved) "Solved before 4clojure started scoring solutions")))))

(defn save-solution [user-id problem-id code]
  (update! :solutions
           {:user user-id
            :problem problem-id}
           {:$set {:code code}}
           :upsert true))
