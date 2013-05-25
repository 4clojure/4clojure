(ns foreclojure.mongo
  (:use somnium.congomongo
        [foreclojure.data-set :only [load-problems]]
        [foreclojure.config   :only [config]]
        [foreclojure.problems :only [number-from-mongo-key solved-stats get-problem-list]]
        [foreclojure.users    :only [get-users]]))

(defn connect-to-db []
  (let [{:keys [db-user db-pwd db-host db-name]} config]
    (mongo!
     :host (or db-host "localhost")
     :db   (or db-name "mydb"))
    (when (and db-user db-pwd)
      (authenticate db-user db-pwd))))

(defn prepare-problems []
  (when-not (fetch-one :problems)
    (load-problems))
  (add-index! :problems [:solved]))

(defn prepare-seqs []
  (update! :seqs
           {:_id "problems"}
           {:$set {:seq (->> (fetch :problems :only [:_id])
                             (map :_id)
                             (apply max)
                             (inc))}}))

;; make it easier to get off the ground by marking contributors automatically
;; useful since some "in-development" features aren't enabled for all users
(defn prepare-users []
  (add-index! :users [:user] :unique true)
  (add-index! :users [[:solved -1]])
  (add-index! :users [:email])
  (update! :users
           {:user {:$in (:contributors config)}}
           {:$set {:contributor true}}
           :upsert false
           :multiple true))

(defn prepare-solutions []
  (add-index! :solutions [:user :problem]))

(defn reconcile-solved-count
  "Overwrites the times-solved field in the problems collection based on data from the users collection. Should only be called on server startup since it isn't a safe operation. Also updates the total-solved agent."
  []
  (let [+ (fnil + 0)
        [raw-scores raw-solved] (for [field [:scores :solved]]
                                  ;; we fetch two separate collections so that it's easy to iterate
                                  ;; over it twice without holding onto the head on the first pass
                                  (mapcat field (fetch :users :only [:scores :solved])))
        scores (->> raw-scores
                    (frequencies)
                    (reduce (fn [scores [[id score] times]]
                              (update-in scores
                                         [(number-from-mongo-key id) score]
                                         + times))
                            {}))
        solved-counts (frequencies (map int raw-solved))
        total (reduce + 0 (vals solved-counts))]
    (send solved-stats (constantly (assoc scores :total total :solved-counts solved-counts)))))

(defn prepare-mongo []
  (connect-to-db)
  (prepare-problems)
  (prepare-seqs)
  (prepare-users)
  (prepare-solutions)
  (reconcile-solved-count))
