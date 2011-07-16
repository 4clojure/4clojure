(ns foreclojure.test.core
  (:use [foreclojure.core]
        [clojure.test]
        [foreclojure.users]
        [foreclojure.problems]
        [foreclojure.mongo]))

(prepare-mongo)

(defn users-solved []
  (reduce #(if-let [v (%1 %2)]
             (assoc %1 %2 (inc v))
             (assoc %1 %2 1))
          {}
          (flatten
           (map :solved (get-users)))))

(defn problems-solved []
  (into
   {}
   (filter
    #(-> % second zero? not)
    (reduce
     #(assoc %1 (:_id %2) (:times-solved %2))
     {}
     (get-problem-list)))))
  

(deftest db-integrity
  (is (= (users-solved) (problems-solved)) "DB integrity check - problems solved"))
