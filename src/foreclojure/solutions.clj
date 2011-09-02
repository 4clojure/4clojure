(ns foreclojure.solutions
  (:require [clojure.string      :as   s])
  (:use     [somnium.congomongo  :only (fetch-one update!)]
            [amalloy.utils.debug :only (?)]))

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
