(ns foreclojure.datatable
  (:require [cheshire.core  :as   json])
  (:use     [foreclojure.ring :only [wrap-json wrap-debug]]
            [foreclojure.users :only [user-datatable-query]]
            [compojure.core :only [routes GET]]))

(def datatable-routes
  (-> (routes
       (GET "/datatable/users" [& more]
         {:body (user-datatable-query more)
          :status 200}))
      (wrap-json)))
