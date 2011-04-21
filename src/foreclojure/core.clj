(ns foreclojure.core
  (:use compojure.core
        [foreclojure static problems login register users]
        ring.adapter.jetty
        somnium.congomongo
        [ring.middleware.reload :only [wrap-reload]]
        [clojure.java.io :only [file]])
  (:require [compojure [route :as route] [handler :as handler]]
            [sandbar.stateful-session :as session]
            [ring.util.response :as response]))

(mongo!
 :db "mydb")
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

(def config
  (binding [*read-eval* false]
    (read-string
     (slurp (file (System/getProperty "user.dir") "config.clj")))))

(def app
  (handler/site
   (session/wrap-stateful-session
    (if (:wrap-reload config)
      (wrap-reload #'main-routes '(foreclojure.core))
      #'main-routes))))

(defn run []
  (run-jetty (var app) {:join? false :ssl? true :port 8080 :ssl-port 8443
                        :keystore "keystore"
                        :key-password "dev_pass"}))

(defn -main [& args]
  (run))
