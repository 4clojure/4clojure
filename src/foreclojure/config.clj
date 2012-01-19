(ns foreclojure.config
  (:use [clojure.java.io :only [file]]
        [clj-config.core :only [safely read-config]]))

(let [cwd (System/getProperty "user.dir")]
  (def config-file (file cwd "config.clj"))
  (def contest-file (file cwd "contest.clj")))

(def config (safely read-config config-file))

;; Defs both for convenience and compile-time verification of simple settings
(def repo-url (or (:repo-url config)
                  (throw (Exception. "config.clj needs a :repo-url key"))))

(letfn [(host [key]
          (get-in config [:hosts key]))]
  (def static-host (host :static))
  (def dynamic-host (host :dynamic))
  (def redirect-hosts (host :redirects))
  (def canonical-host (or dynamic-host "www.4clojure.com")))

(letfn [(periodic-check [period f]
          (let [data (atom [(f) 0])]
            (fn []
              (let [now (System/currentTimeMillis)]
                (first ;; get (f) value out of swap result
                 (swap! data (fn [[value last-checked :as prev]]
                               (if (> period (- now last-checked))
                                 prev ;; not time to re-check yet
                                 [(f) now]))))))))]
  (def contest (periodic-check 5000 #(try (-> (safely read-config contest-file)
                                              (doto str)) ; reading {x} doesn't cause a fast
                                                          ; error, but printing it does
                                          (catch Throwable _ nil)))))
