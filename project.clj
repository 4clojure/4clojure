(defproject foreclojure "0.1.0-SNAPSHOT"
  :description "4clojure - a website for lisp beginners"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.2"]
                 [hiccup "0.2.4"]
                 [clojail "0.4.0-SNAPSHOT"]
                 [sandbar "0.4.0-SNAPSHOT"]
                 [congomongo "0.1.3-SNAPSHOT"]
                 [org.jasypt/jasypt "1.7"]
                 [amalloy/utils "[0.3.7,)"]]
  :dev-dependencies [[lein-ring "0.4.0"]
                     [swank-clojure "1.2.1"]]
  :main foreclojure.core
  :ring {:handler foreclojure.core/app})
