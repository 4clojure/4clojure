(defproject foreclojure "0.3.1"
  :description "4clojure - a website for lisp beginners"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.2"]
                 [hiccup "0.2.4"]
                 [clojail "0.4.0-SNAPSHOT"]
                 [sandbar "0.4.0-SNAPSHOT"]
                 [org.clojars.christophermaier/congomongo "0.1.4-SNAPSHOT"]
                 [org.jasypt/jasypt "1.7"]
                 [amalloy/utils "[0.3.7,)"]
                 [clj-github "1.0.1"]
                 [ring "0.3.7"]
                 [clj-config "0.1.0"]
                 [incanter/incanter-core "1.2.3"]
                 [incanter/incanter-charts "1.2.3"]
                 [org.apache.commons/commons-email "1.2"]]
  :dev-dependencies [[lein-ring "0.4.0"]
                     [swank-clojure "1.2.1"]]
  :main foreclojure.core
  :ring {:handler foreclojure.core/app})
