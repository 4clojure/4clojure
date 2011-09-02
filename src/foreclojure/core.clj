(ns foreclojure.core
  (:require [compojure.route            :as   route]
            [compojure.handler          :as   handler]
            [sandbar.stateful-session   :as   session])
  (:use     [compojure.core             :only (defroutes GET)]
            [foreclojure.static         :only (static-routes welcome-page)]
            [foreclojure.problems       :only (problems-routes)]
            [foreclojure.login          :only (login-routes)]
            [foreclojure.register       :only (register-routes)]
            [foreclojure.golf           :only (golf-routes)]
            [foreclojure.ring           :only (resources wrap-strip-trailing-slash wrap-url-as-file)]
            [foreclojure.users          :only (users-routes)]
            [foreclojure.config         :only (config)]
            [foreclojure.social         :only (social-routes)]
            [foreclojure.version        :only (version-routes)]
            [foreclojure.graphs         :only (graph-routes)]
            [foreclojure.mongo          :only (prepare-mongo)]
            [foreclojure.utils          :only (wrap-uri-binding)]
            [ring.adapter.jetty         :only (run-jetty)]
            [ring.middleware.reload     :only (wrap-reload)]
            [ring.middleware.stacktrace :only (wrap-stacktrace)]
            [ring.middleware.file-info  :only (wrap-file-info)]
            [ring.middleware.gzip       :only (wrap-gzip)]))

(def *block-server* false)

(defroutes main-routes
  (GET "/" [] (welcome-page))
  login-routes
  register-routes
  problems-routes
  users-routes
  static-routes
  social-routes
  version-routes
  graph-routes
  golf-routes
  (-> (resources "/*")
      (wrap-url-as-file)
      (wrap-file-info))
  (route/not-found "Page not found"))

(def app (-> #'main-routes
             ((if (:wrap-reload config)
                #(wrap-reload % '(foreclojure.core))
                identity))
             session/wrap-stateful-session
             handler/site
             wrap-uri-binding
             wrap-strip-trailing-slash
             wrap-gzip))

(defn run []
  (prepare-mongo)
  (run-jetty (var app) {:join? *block-server* :port 8080}))

(defn -main [& args]
  (binding [*block-server* true]
    (run)))