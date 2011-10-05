(ns foreclojure.api
  (:require [cheshire.core  :as   json])
  (:use     [foreclojure.ring :only [wrap-json wrap-debug]]
            [foreclojure.utils :only [as-int]]
            [compojure.core :only [routes GET]]
            [somnium.congomongo       :only    [fetch-one]]
            [useful.map :only [update-each]]))

(def api-routes
  (-> (routes
       (GET "/api/problem/:id" [id]
         (when-let [problem (fetch-one :problems :where {:_id (as-int id)
                                                         :approved true})]
           {:body (-> problem
                      (dissoc :_id :approved)
                      (update-each [:restricted :tags]
                                   #(or % ())))})))
      (wrap-json)))
