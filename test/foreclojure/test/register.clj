(ns foreclojure.test.register
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response])
  (:use [foreclojure.register])
  (:use [clojure.test])
  (:use [midje.sweet])
  (:use [foreclojure.utils :only [form-row assuming flash-error]])
  (:use [somnium.congomongo :only [insert! fetch-one]]))
  



(deftest test-do-register
  (let [uname "username"
        lngname "thisisalongusername"
        bname "$#%^$djc"
        pwd "password"
        shpwd "pass"
        email "test@test.com"
        bemail "testing.com"]
    (against-background [(fetch-one :users :where {:user lngname}) => nil
                         (fetch-one :users :where {:user bname}) => nil
                         (fetch-one :users :where {:user uname}) => nil
                         (fetch-one :users :where {:email email}) => nil
                         (insert! :users anything) => nil
                         (session/session-put! :user anything) => nil
                         (session/flash-put! :user anything) => nil
                         (session/flash-put! :email anything) => nil]
      (fact "about do-register - good inputs"
          (do-register uname pwd pwd email) => truthy
            (provided 
              (response/redirect "/") => 1))
      (fact "about do-register - userexists"
          (do-register uname pwd pwd email) => truthy
            (provided 
              (fetch-one :users :where {:user uname}) => {:user "username"}
              (flash-error "/register" "User already exists") => 1))
      (fact "about do-register - username too long"
          (do-register lngname pwd pwd email) => truthy
            (provided 
              (flash-error "/register" "Username must be 4-13 characters long") => 1))
      (fact "about do-register - username not alphanumeric"
          (do-register bname pwd pwd email) => truthy
            (provided 
              (flash-error "/register" "Username must be alphanumeric") => 1))
      (fact "about do-register - short password"
          (do-register uname shpwd shpwd email) => truthy
            (provided 
              (flash-error "/register" "Password must be at least seven characters long") => 1))
      (fact "about do-register - passwords don't match"
          (do-register uname pwd shpwd email) => truthy
            (provided 
              (flash-error "/register" "Passwords don't match") => 1))
      (fact "about do-register - bad emal"
          (do-register uname pwd pwd bemail) => truthy
            (provided 
              (flash-error "/register" "Please enter a valid email address") => 1))
      (fact "about do-register - email exists"
          (do-register uname pwd pwd email) => truthy
            (provided 
              (fetch-one :users :where {:user uname}) => nil
              (fetch-one :users :where {:email email}) => {:user "username"}
              (flash-error "/register" "User with this email address already exists") => 1)))))

