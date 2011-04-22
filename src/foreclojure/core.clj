(ns foreclojure.core
  (:use compojure.core
        [foreclojure static problems login register users]
        ring.adapter.jetty
        somnium.congomongo
        [ring.middleware.reload :only [wrap-reload]]
        [clojure.java.io :only [file]]
        [clj-config.core :only [safely get-key]])
  (:require [compojure [route :as route] [handler :as handler]]
            [sandbar.stateful-session :as session]
            [ring.util.response :as response]))

(def config-file (file (System/getProperty "user.dir") "config.clj"))

(mongo!
 :host (if-let [host (safely get-key config-file :db-host)]
         host
         "localhost")
 :db "mydb")

(if-let [db-user (safely get-key config-file :db-user)]
  (if-let [db-pwd  (safely get-key config-file :db-pwd)]
    (authenticate db-user db-pwd)))

(add-index! :users [:user] :unique true)
(add-index! :users [[:solved -1]])

(defroutes main-routes
  (GET "/" [] (welcome-page))
  login-routes
  register-routes
  problems-routes
  users-routes
  static-routes
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site
   (session/wrap-stateful-session
    (if (safely get-key config-file :wrap-reload)
      (wrap-reload #'main-routes '(foreclojure.core))
      #'main-routes))))

(defn run []
  (run-jetty (var app) {:join? false :port 8080}))

(defn -main [& args]
  (run))
