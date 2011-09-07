(ns foreclojure.graphs
  (:require [incanter.charts         :as   chart]
            [incanter.core           :as   incanter]
            [incanter.stats          :as   stats])
  (:import  [java.io                 ByteArrayInputStream
                                     ByteArrayOutputStream])
  (:use     [compojure.core          :only [defroutes GET]]
            [foreclojure.utils       :only [from-mongo]]
            [somnium.congomongo      :only [fetch-one]]
            [useful.utils            :only [with-adjustments]]))

(defn un-group
  "Turn a compact set of [data-point num-repetitions] pairs into a
  bunch of repeated data points so that incanter will make a histogram
  of them."
  [frequencies]
  (apply concat (for [[x count] frequencies]
                  (repeat count x))))

(defn fetch-score-frequencies [problem-id]
  (into {}
        (for [[k v] (:scores
                     (from-mongo
                      (fetch-one :problems
                                 :where {:_id problem-id}
                                 :only [:scores])))]
          [(Integer/parseInt (name k)), v])))

(defn make-problem-plot [id best curr]
  (let [freqs (fetch-score-frequencies id)
        chart-data (un-group freqs)
        [best curr] [(or best curr) (or curr best)]
        chart (chart/histogram chart-data
                               :title (str "League scores: problem " id)
                               :x-label "Solution length"
                               :y-label "How often"
                               :nbins 25)]
    (when best
      (chart/add-pointer chart best 0
                         :text "your best"
                         :angle :ne))
    (when (and curr (not= curr best))
      (chart/add-pointer chart curr 0
                         :text "this"
                         :angle :ne))
    (when-not (> (count freqs) 1)
      (chart/add-text chart best (freqs best 0)
                      "Very little golfing data - chart may suck"))
    (doto chart (chart/set-theme :bw))))

(defn serve-plot [plot]
  (let [out (ByteArrayOutputStream.)
        in (do
             (incanter/save plot out)
             (ByteArrayInputStream. (.toByteArray out)))]
    {:response 200
     :headers {"Content-Type" "image/png"}
     :body in}))

(defroutes graph-routes
  (GET "/leagues/golf/:id" [id best curr]
    (with-adjustments #(when (seq %) (Integer/parseInt %)) [id best curr]
      (serve-plot (make-problem-plot id best curr)))))

