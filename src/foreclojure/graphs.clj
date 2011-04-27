(ns foreclojure.graphs
  (:use compojure.core
        (foreclojure utils)
        somnium.congomongo
        (amalloy.utils [transform :only [with-adjustments]]
                       [reorder :only [reorder]]))
  (:require (incanter [charts :as chart]
                      [core :as incanter]
                      [stats :as stats]))
  (:import (java.io ByteArrayInputStream
                    ByteArrayOutputStream)))

(defn un-group
  "Turn a compact set of [data-point num-repetitions] pairs into a
  bunch of repeated data points so that incanter will make a histogram
  of them."
  [frequencies]
  (mapcat (partial apply (reorder repeat))
          frequencies))

(defn fetch-score-frequencies [problem-id]
  (into {}
        (for [[k v] (:scores
                     (from-mongo
                      (fetch-one :problems
                                 :where {:_id problem-id}
                                 :only [:scores])))]
          [(Integer/parseInt (name k)), v])))

(defn make-problem-plot [id best prev]
  (let [freqs (fetch-score-frequencies id)
        chart-data (un-group freqs)
        best (or best prev)
        chart (chart/histogram chart-data
                               :title (str "League scores: problem " id)
                               :x-label "Solution length"
                               :y-label "How often")]
    (doto chart
;;      (chart/set-theme :dark)
      (chart/add-pointer best (freqs best 0)
                         :text "best"
                         :angle :south)
      (chart/add-pointer prev (freqs prev 0)
                         :text "this"
                         :angle :south))))

(defn serve-plot [plot]
  (let [out (ByteArrayOutputStream.)
        in (do
             (incanter/save plot out)
             (ByteArrayInputStream. (.toByteArray out)))]
    {:response 200
     :headers {"Content-Type" "image/png"}
     :body in}))

(defroutes graph-routes
  (GET "/leagues/golf/:id" [id best prev]
    (with-adjustments #(when % (Integer/parseInt %)) [id best prev]
      (serve-plot (make-problem-plot id best prev)))))

