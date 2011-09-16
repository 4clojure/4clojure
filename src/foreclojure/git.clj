(ns foreclojure.git
  (:require [clojure.string :as s])
  (:use [clojure.java.shell :only [sh]]))

(letfn [(cmd [& args]
          (not-empty (s/trim (:out (apply sh args)))))]

  ;; fetch these at load time rather than on demand, so that it's accurate even
  ;; if someone checks out a different revision to poke at without restarting
  ;; the server (eg to diagnose bugs in a release)
  (def sha (cmd "git" "rev-parse" "--verify" "HEAD"))
  (def tag (cmd "git" "describe" "--abbrev=0" "master")))
