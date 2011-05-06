(ns foreclojure.config
  (:use [clojure.java.io :only [file]]
        [clj-config.core :only [safely read-config]]))

(def config-file (file (System/getProperty "user.dir") "config.clj"))

(def config (safely read-config config-file))
