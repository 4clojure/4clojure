(ns foreclojure.ring
  (:use [compojure.core :only [GET]]
        [ring.util.response :only [response]])
  (:require [clojure.java.io :as io]
            [clojure.string :as s])
  (:import (java.net URL)))

;; copied from compojure.route, modified to use File instead of Stream
(defn resources
  "A route for serving resources on the classpath. Accepts the following
  keys:
    :root - the root prefix to get the resources from. Defaults to 'public'."
  [path & [options]]
  (-> (GET path {{resource-path :*} :route-params}
        (let [root (:root options "public")]
          (when-let [res (io/resource (str root "/" resource-path))]
            (response (io/as-file res)))))))

(defn wrap-url-as-file [handler]
  (fn [request]
    (when-let [{body :body :as resp} (handler request)]
      (if (and (instance? URL body)
               (= "file" (.getProtocol ^URL body)))
        (update-in resp [:body] io/as-file)
        resp))))

(defn wrap-strip-trailing-slash [handler]
  (fn [request]
    (handler (update-in request [:uri] s/replace #"(?<=.)/$" ""))))
