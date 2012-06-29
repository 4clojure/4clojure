(ns foreclojure.config
  (:require [clojure.java.io :refer [file]]
            [useful.config :refer [load-config]]))

(def config (load-config "config.clj"))

;; Defs both for convenience and compile-time verification of simple settings
(def repo-url (or (:repo-url config)
                  (throw (Exception. "config.clj needs a :repo-url key"))))

(letfn [(host [key]
          (get-in config [:hosts key]))]
  (def static-host (host :static))
  (def dynamic-host (host :dynamic))
  (def redirect-hosts (host :redirects))
  (def canonical-host (or dynamic-host "www.4clojure.com")))
