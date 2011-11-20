(ns foreclojure.test.users
  (:use [foreclojure.users] :reload)
  (:use [clojure.test])
  (:use [midje.sweet]))

(def users [{:user "user1", :solved [1 2 3 4] :rank 1}
              {:user "user2", :solved [1 2 3] :rank 2}
              {:user "user3", :solved [1 2] :rank 3}
              {:user "user4", :solved [1] :rank 4}
              {:user "user5", :solved [1] :rank 5}
              {:user "user6", :solved [1] :rank 6}
              {:user "user7", :solved [1] :rank 7}
              {:user "user8", :solved [1] :rank 8}
              {:user "user9", :solved [1] :rank 9}
              {:user "user10", :solved [1] :rank 10}
              ])


(deftest user-datatables-paging
  (facts "about datatable-paging"
         (count (datatable-paging 0 10 users)) => 10
         (count (datatable-paging 8 10 users)) => 2
         (count (datatable-paging 2 5 users))) => 5
         (:user (first (datatable-paging 2 5 users))) => "user3"
         (:user (last (datatable-paging 2 5 users))) => "user7")

(deftest user-datatables-sort-cols
  (facts "about datatable sorting by columns"
         (:user (first (datatable-sort-cols 0 users))) => "user1"
         (:user (second (datatable-sort-cols 1 users))) => "user10"
         (:user (last (datatable-sort-cols 2 users))) => "user1"))

(deftest user-datatables-sort-dir
  (facts "about datatable sort direction"
         (:user (first (datatable-sort-dir "asc" users))) => "user1"
         (:user (first (datatable-sort-dir "desc" users))) => "user10"))

(deftest user-datatables-sort
  (facts "about sorting by column and direction combined"
         (:user (first (datatable-sort 1 "asc" users))) => "user1"
         (:user (first (datatable-sort 1 "desc" users))) => "user9"))

(deftest user-datatables-filter
  (facts "about filtering the username by text"
         (:user (first (datatable-filter "4" users))) => "user4"
         (:user (second (datatable-filter "1" users))) => "user10"
         (count (datatable-filter nil users)) => 10))
