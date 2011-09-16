(ns foreclojure.test.users
  (:use [foreclojure.users] :reload)
  (:use [clojure.test])
  (:use [midje.sweet]))


(deftest sorting-by-solved
  (def users-by-solved
    [{:user "user1" :solved [1] } {:user "user2" :solved [1 2 3 4]}
     {:user "user3" :solved [2 2] } {:user "user4" :solved [3]}])
  (def users-sorted-by-solved (users-sort users-by-solved))
  
  (fact
   (:user (first users-sorted-by-solved)) => "user2")
  (fact
   (:user (last users-sorted-by-solved)) => "user1"))

(deftest sorting-by-last-login-date
  (def date-formatter (java.text.SimpleDateFormat. "MM/dd/yyyy"))
  (def date1 (.parse date-formatter "11/01/2001"))
  (def date2 (.parse date-formatter "8/1/2010"))
  (def users-by-date
    [{:user "user1" :last-login date1  } {:user "user2" :last-login date2}
     {:user "user3" } {:user "user4" :last-login date1}])
  (sort-by :last-login users-by-date)
  (def users-sorted-by-date (users-sort users-by-date))
  
  (fact
   (:user (first users-sorted-by-date)) => "user2")
  (fact
   (:user (last users-sorted-by-date)) => "user3"))

(deftest test-user-with-ranking
  (def users [{:user "user1", :solved [1 2 3 4]}
              {:user "user2", :solved [1 2 3]}
              {:user "user3", :sovled [1 2]}
              {:user "user4", :solved [1]}])

  
  (facts "about user"
         (:rank (get-user-with-ranking "user1" users)) => "1 out of 4"
         (:rank (get-user-with-ranking "user2" users)) => "2 out of 4"
         (:rank (get-user-with-ranking "user3" users)) => "3 out of 4"
         (:rank (get-user-with-ranking "user4" users)) => "4 out of 4" ))

