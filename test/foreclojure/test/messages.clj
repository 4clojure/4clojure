(ns foreclojure.test.messages
  (:use [foreclojure.messages     :only [err-msg]])
  (:use [clojure.test])
  (:use [midje.sweet]))

(def filler "BAKE ME COOKIES")

(deftest test-err-msg
   (fact "about err-msg - format" 
      (err-msg "security.login-required" filler) => "You must BAKE ME COOKIES to do this")
   (fact "about err-msg - standard"
      (err-msg "settings.user-exists") => "User already exists"))