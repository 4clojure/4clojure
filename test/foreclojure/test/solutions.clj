(ns foreclojure.test.solutions
  (:require [clojure.string       :as   s])
  (:use     [somnium.congomongo   :only [fetch-one update!]])
  (:use     [useful.debug         :only [?]])            
  (:use     [foreclojure.messages :only [err-msg]])
  (:use     [foreclojure.solutions])
  (:use     [clojure.test])
  (:use     [midje.sweet]))

(deftest test-get-solution
  "this test covers the getting solutions to display"
  (let [uid 1
        pid 1
        code "I AM SOME COOL CODE"]
    (fact "about get-solution: private" 
      (get-solution :private uid pid) => code
      (provided
        (fetch-one :solutions :where {:user uid :problem pid}) => {:code code}))
    (fact "about get-solution: hide solutions = true"
      (get-solution :public uid pid) => falsey
      (provided
       (fetch-one :users :where {:_id uid} :only [:hide-solutions]) => {:hide-solutions true}))
    (fact "about get-solution: hide solutions = true"
      (get-solution :public uid pid) => code
      (provided
        (fetch-one :solutions :where {:user uid :problem pid}) => {:code code}
        (fetch-one :users :where {:_id uid} :only [:hide-solutions]) => {:hide-solutions false}))
    (fact "about get-solution: scored early"
      (get-solution uid pid) => (err-msg "solution.scored-early" pid)
      (provided
        (fetch-one :solutions :where {:user uid, :problem pid}) => {}
        (fetch-one :users 
                   :where {:_id uid} 
                   :only [(keyword (str "scores." pid)) 
                          :solved]) => {:scores {pid pid} :solved []}))
    (fact "about get-solution: solved early"
      (get-solution uid pid) => (err-msg "solution.solved-early")
      (provided
        (fetch-one :solutions :where {:user uid, :problem pid}) => {}
        (fetch-one :users 
                   :where {:_id uid} 
                   :only [(keyword (str "scores." pid)) 
                          :solved]) => {:scores {} :solved [pid]}))))
