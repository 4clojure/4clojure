(ns foreclojure.core
  (:require [compojure.route            :as   route]
            [compojure.handler          :as   handler]
            [foreclojure.config         :as   config]
            [noir.session               :as   session])
  (:import  [java.lang                  OutOfMemoryError])
  (:use     [compojure.core             :only [defroutes routes GET]]
            [foreclojure.static         :only [static-routes]]
            [foreclojure.api            :only [api-routes]]
            [foreclojure.datatable      :only [datatable-routes]]
            [foreclojure.problems       :only [problems-routes]]
            [foreclojure.login          :only [login-routes]]
            [foreclojure.settings       :only [settings-routes]]
            [foreclojure.register       :only [register-routes]]
            [foreclojure.golf           :only [golf-routes]]
            [foreclojure.ring           :only [resources wrap-strip-trailing-slash wrap-url-as-file wrap-versioned-expiry split-hosts wrap-404 wrap-debug]]
            [foreclojure.users          :only [users-routes]]
            [foreclojure.config         :only [config]]
            [foreclojure.social         :only [social-routes]]
            [foreclojure.version        :only [version-routes]]
            [foreclojure.graphs         :only [graph-routes]]
            [foreclojure.mongo          :only [prepare-mongo]]
            [foreclojure.ring-utils     :only [wrap-request-bindings]]
            [foreclojure.periodic       :only [schedule-task]]
            [foreclojure.home           :only [home-routes]]
            [ring.adapter.jetty         :only [run-jetty]]
            [ring.middleware.reload     :only [wrap-reload]]
            [ring.middleware.stacktrace :only [wrap-stacktrace]]
            [ring.middleware.file-info  :only [wrap-file-info]]
            [ring.middleware.gzip       :only [wrap-gzip]]
            [mongo-session.core         :only [mongo-session]]))

(def ^:dynamic *block-server* false)

(defroutes resource-routes
  (-> (resources "/*")
      (wrap-url-as-file)
      (wrap-file-info)
      (wrap-versioned-expiry)))

(def dynamic-routes
  (-> (routes home-routes
              login-routes
              register-routes
              problems-routes
              users-routes
              static-routes
              social-routes
              version-routes
              graph-routes
              api-routes
              datatable-routes
              golf-routes
              settings-routes)
      ((if (:wrap-reload config)
         #(wrap-reload % '(foreclojure.core))
         identity))
      session/wrap-noir-flash
      (session/wrap-noir-session {:store (mongo-session :sessions)})
      wrap-request-bindings
      handler/site
      wrap-strip-trailing-slash))

(defn redirect-routes [request]
  (let [{:keys [scheme uri]} request
        proper-uri (str (name scheme)
                        "://"
                        config/canonical-host
                        uri)]
    {:status 302
     :headers {"Location" proper-uri}
     :body (str "<a href='" proper-uri "'>"
                proper-uri
                "</a>")}))

(def host-handlers (reduce into
                           {:default (routes dynamic-routes resource-routes)}
                           [(for [host config/redirect-hosts]
                              [host redirect-routes])
                            (for [[host route] [[config/static-host resource-routes]
                                                [config/dynamic-host dynamic-routes]]
                                  :when host]
                              [host route])]))

(def app (-> (split-hosts host-handlers)
             wrap-404
             wrap-gzip))

(defn register-heartbeat []
  (when-let [period (:heartbeat config)]
    (apply schedule-task
           (let [^java.io.PrintWriter out *out*
                 ^Runtime r (Runtime/getRuntime)]
             (fn []
               (.println out (format "%d/%d/%d MB free/total/max"
                                     (int (/ (. r (freeMemory)) 1e6))
                                     (int (/ (. r (totalMemory)) 1e6))
                                     (int (/ (. r (maxMemory)) 1e6))))))
           period)))

(let [default-jetty-port 8080]
  (defn run []
    (prepare-mongo)
    (register-heartbeat)
    (letfn [(run []
              (run-jetty (var app) {:join? *block-server*
                                    :port  (get config :jetty-port
                                                default-jetty-port)}))]
      (if *block-server*
        (while true
          (try
            (run)
            (catch OutOfMemoryError _
              (binding [*out* *err*]
                (println "Caught error at " (java.util.Date.))))))
        (run)))))

(defn -main [& args]
  (binding [*block-server* true]
    (run)))
