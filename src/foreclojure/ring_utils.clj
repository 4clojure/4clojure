(ns foreclojure.ring-utils
  (:require [foreclojure.config :as config]))

(def ^{:dynamic true} *url*         nil) ; url of current request
(def ^{:dynamic true} *host*        nil) ; Host header sent by client
(def ^{:dynamic true} *http-scheme* nil) ; keyword, :http or :https

(defn get-host [request]
  (get-in request [:headers "host"]))

(defn wrap-request-bindings [handler]
  (fn [req]
    (binding [*url* (:uri req)
              *host* (or (get-host req) config/canonical-host)
              *http-scheme* (:scheme req)]
      (handler req))))

(letfn [(url-fn [host]
          (if host
            #(str (name (or *http-scheme* :http)) "://" host "/" %)
            #(str "/" %)))]
  (def universal-url (url-fn (or config/static-host config/canonical-host)))
  (def static-url    (url-fn config/static-host)))
