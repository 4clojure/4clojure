(ns foreclojure.core
  (:use compojure.core
        [foreclojure static problems login register
         users config social]
        ring.adapter.jetty
        somnium.congomongo
        [ring.middleware.reload :only [wrap-reload]])
  (:require [compojure [route :as route] [handler :as handler]]
            [sandbar.stateful-session :as session]
            [ring.util.response :as response]))

(mongo!
 :host (if-let [host (:db-host config)]
         host
         "localhost")
 :db "mydb")

(if-let [db-user (:db-user config)]
  (if-let [db-pwd (:db-pwd config)]
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
  social-routes
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site
   (session/wrap-stateful-session
    (if (:wrap-reload config)
      (wrap-reload #'main-routes '(foreclojure.core))
      #'main-routes))))

(defn run []
  (run-jetty (var app) {:join? false :port 8080}))

(defn -main [& args]
  (run))