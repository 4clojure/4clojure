(ns foreclojure.db-utils
  (:use [foreclojure.problems]
        [foreclojure.users]
        [somnium.congomongo]))

(defn reconcile-solved-count
  "Overwrites the times-solved field in the problems collection based on data from the users collection. Should only be called on server startup since it isn't a safe operation. Also updates the total-solved agent."
  []
  (send
   total-solved +
   (let [problems (get-problem-list)]
     (reduce
      #(do
         (update! :problems
                  {:_id (first %2)}
                  {:$set {:times-solved (last %2)}})
         (+ %1 (last %2)))
      0
      (reduce #(update-in %1 [%2] inc)
              (reduce #(conj %1 [%2 0])
                      {}
                      (map :_id problems))
              (mapcat :solved (get-users)))))))