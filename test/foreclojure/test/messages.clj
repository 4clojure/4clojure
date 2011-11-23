(ns foreclojure.test.messages
  (:use [foreclojure.messages     :only [err-msgs]])
  (:use [clojure.test])
  (:use [midje.sweet]))

(deftest test-err-msgs
   (fact "about err-msgs - format" 
      (err-msgs "security.login-required" "LOGIN") => "You must LOGIN to do this")
   (fact "about err-msgs - standard"
      (err-msgs "settings.user-exists") => "User already exists"))