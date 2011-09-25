#!/bin/bash

java -cp `lein classpath` clojure.main -e \
    "(do (require 'foreclojure.core) (foreclojure.core/-main))"
