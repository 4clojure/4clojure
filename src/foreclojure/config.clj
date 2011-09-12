(ns foreclojure.config
  (:use [clojure.java.io :only [file]]
        [clj-config.core :only [safely read-config]]))

(def config-file (file (System/getProperty "user.dir") "config.clj"))

(def config (safely read-config config-file))

;; Defs both for convenience and compile-time verification of simple settings
(def repo-url (or (:repo-url config)
                  (throw (Exception. "config.clj needs a :repo-url key"))))
