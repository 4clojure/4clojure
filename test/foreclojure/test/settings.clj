               (ns foreclojure.test.settings
  (:require [sandbar.stateful-session :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [foreclojure.settings])
  (:use [clojure.test])
  (:use [midje.sweet])
  (:use [foreclojure.utils :only [get-user assuming flash-error flash-msg]])
  (:use [somnium.congomongo :only [update! fetch-one]]))
  



(deftest test-do-update-settings
   "This test covers the control flow and validation of updating user settings" 
  (let [old-name "username"
        new-name "usernamenew"
        old-pwd "oldpasswd"
        new-pwd "password"
        or (StrongPasswordEncryptor.)
        enpwd (.encryptPassword or old-pwd)
        lngname "thisisalongusername"
        bname "$#%^$djc"
        short-pwd "pass"
        email "test@test.com"
        bad-email "testing.com"]
    ;setting up defaults for function mocking
    (against-background [(fetch-one :users :where {:user lngname}) => nil
                         (fetch-one :users :where {:user bname}) => nil
                         (fetch-one :users :where {:user new-name}) => nil
                         (fetch-one :users :where {:email email :user {:$ne old-name}}) => nil
                         (update! :users anything anything :upsert false) => nil
                         (session/session-put! :user anything) => nil
                         (session/session-get :user) => old-name
                         (get-user old-name) => {:user old-name :pwd enpwd}]
      (fact "about do-update-settings! - good inputs"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              (flash-msg "/problems" anything) => 1))
      (fact "about do-update-settings! - userexists"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              (fetch-one :users :where {:user new-name}) => {:user "username-new"}
              (flash-error "/settings" "User already exists") => 1))
      (fact "about do-update-settings! - username too long"
          (do-update-settings! lngname old-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              (flash-error "/settings" "Username must be 4-13 characters long") => 1))
      (fact "about do-update-settings! - username not alphanumeric"
          (do-update-settings! bname old-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              (flash-error "/settings" "Username must be alphanumeric") => 1))
      (fact "about do-update-settings! - short password"
          (do-update-settings! new-name old-pwd short-pwd short-pwd email false false) => truthy
            (provided 
              (flash-error "/settings" "New password must be at least seven characters long") => 1))
      (fact "about do-update-settings! - passwords don't match"
          (do-update-settings! new-name old-pwd new-pwd old-pwd email false false) => truthy
            (provided 
              (flash-error "/settings" "New password was not entered identically twice") => 1))
      (fact "about do-update-settings! - old password doesn't match"
          (do-update-settings! new-name new-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              (flash-error "/settings" "Current password incorrect") => 1))
      (fact "about do-update-settings! - bad emal"
          (do-update-settings! new-name old-pwd new-pwd new-pwd bad-email false false) => truthy
            (provided 
              (flash-error "/settings" "Please enter a valid email address") => 1))
      (fact "about do-update-settings! - email exists"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false) => truthy
            (provided 
              ;you have to specify both because midje can't tell them apart
              (fetch-one :users :where {:user new-name}) => nil
              (fetch-one :users :where {:email email :user {:$ne old-name}}) => {:user old-name}
              (flash-error "/settings" "User with this email address already exists") => 1)))))

