(ns foreclojure.version-utils
  (:use     [foreclojure.ring-utils :only [static-url]])
  (:require [foreclojure.git        :as   git]
            [foreclojure.config     :as   config]
            [clojure.string         :as   string]
            [hiccup.page            :as   hiccup]))

(let [version-suffix (str "__" git/tag)]
  (defn add-version-number [file]
    (let [[_ path ext] (re-find #"(.*)\.(.*)$" file)]
      (str path version-suffix "." ext)))

  (defn strip-version-number [file]
    (string/replace file version-suffix "")))

(letfn [(wrap-versioning [f]
          (fn [& files]
            (for [file files]
              (f (static-url (add-version-number file))))))]
  (def js  (wrap-versioning hiccup/include-js))
  (def css (wrap-versioning hiccup/include-css)))