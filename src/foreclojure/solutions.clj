(ns foreclojure.solutions
  (:use foreclojure.utils
        somnium.congomongo
        (amalloy.utils [debug :only [?]]))
  (:require [clojure.string :as s]))

(defn get-solution [user-id problem-id]
  (:code (fetch-one :solutions
                    :where {:user user-id
                            :problem problem-id})))

(defn save-solution [user-id problem-id code]
  (update! :solutions
           {:user user-id
            :problem problem-id}
           {:$set {:code code}}
           :upsert true))
