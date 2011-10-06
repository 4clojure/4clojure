(ns foreclojure.test.users
  (:use [foreclojure.users] :reload)
  (:use [clojure.test])
  (:use [midje.sweet]))

(def users [{:user "user1", :solved [1 2 3 4] :rank 1}
              {:user "user2", :solved [1 2 3] :rank 2}
              {:user "user3", :sovled [1 2] :rank 3}
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
         (count (datatable-paging users 0 10)) => 10
         (count (datatable-paging users 8 10)) => 2
         (count (datatable-paging users 2 5))) => 5
         (:user (first (datatable-paging users 2 5))) => "user3"
         (:user (last (datatable-paging users 2 5))) => "user7")

(deftest user-datatables-sort-cols
  (facts "about datatable sorting by columns"
         (:user (first (datatable-sort-cols users 0))) => "user1"
         (:user (second (datatable-sort-cols users 1))) => "user10"
         (:user (first (datatable-sort-cols users 2))) => "user1"
         (:user (first (datatable-sort-cols users 3))) => "user1"))

(deftest user-datatables-sort-dir
  (facts "about datatable sort direction"
         (:user (first (datatable-sort-dir users "asc"))) => "user1"
         (:user (first (datatable-sort-dir users "desc"))) => "user10"))

(deftest user-datatables-sort
  (facts "about sorting by column and direction combined"
         (:user (first (datatable-sort users 1 "asc"))) => "user1"
         (:user (first (datatable-sort users 1 "desc"))) => "user9"))

