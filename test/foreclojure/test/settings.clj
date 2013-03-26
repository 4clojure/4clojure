(ns foreclojure.test.settings
  (:require [noir.session             :as   session]
            [ring.util.response       :as   response])
  (:import  [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [foreclojure.settings])
  (:use [foreclojure.messages     :only [err-msg]])
  (:use [clojure.test])
  (:use [midje.sweet])
  (:use [foreclojure.utils :only [get-user assuming flash-error flash-msg default-theme]])
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
                         (session/put! :user anything) => nil
                         (session/get :user) => old-name
                         (get-user old-name) => {:user old-name :pwd enpwd}]
      (fact "about do-update-settings! - good inputs"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              (flash-msg "/problems" anything) => 1))
      (fact "about do-update-settings! - userexists"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              (fetch-one :users :where {:user new-name}) => {:user "username-new"}
              (flash-error "/settings" (err-msg "settings.user-exists")) => 1))
      (fact "about do-update-settings! - username too long"
          (do-update-settings! lngname old-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.uname-size")) => 1))
      (fact "about do-update-settings! - username not alphanumeric"
          (do-update-settings! bname old-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.uname-alphanum")) => 1))
      (fact "about do-update-settings! - short password"
          (do-update-settings! new-name old-pwd short-pwd short-pwd email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.npwd-size")) => 1))
      (fact "about do-update-settings! - passwords don't match"
          (do-update-settings! new-name old-pwd new-pwd old-pwd email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.npwd-match")) => 1))
      (fact "about do-update-settings! - old password doesn't match"
          (do-update-settings! new-name new-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.pwd-incorrect")) => 1))
      (fact "about do-update-settings! - bad email"
          (do-update-settings! new-name old-pwd new-pwd new-pwd bad-email false false default-theme) => truthy
            (provided 
              (flash-error "/settings" (err-msg "settings.email-invalid")) => 1))
      (fact "about do-update-settings! - email exists"
          (do-update-settings! new-name old-pwd new-pwd new-pwd email false false default-theme) => truthy
            (provided 
              ;you have to specify both because midje can't tell them apart
              (fetch-one :users :where {:user new-name}) => nil
              (fetch-one :users :where {:email email :user {:$ne old-name}}) => {:user old-name}
              (flash-error "/settings" (err-msg "settings.email-exists")) => 1)))))

