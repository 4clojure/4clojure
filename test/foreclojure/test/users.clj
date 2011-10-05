(ns foreclojure.test.users
  (:use [foreclojure.users] :reload)
  (:use [clojure.test])
  (:use [midje.sweet]))


(deftest user-datatables-paging
  (def users [{:user "user1", :solved [1 2 3 4]}
              {:user "user2", :solved [1 2 3]}
              {:user "user3", :sovled [1 2]}
              {:user "user4", :solved [1]}
              {:user "user5", :solved [1]}
              {:user "user6", :solved [1]}
              {:user "user7", :solved [1]}
              {:user "user8", :solved [1]}
              {:user "user9", :solved [1]}
              {:user "user10", :solved [1]}
              ])
  (facts "about datatable-paging"
         (count (datatable-paging 0 10 users)) => 10
         (count (datatable-paging 8 10 users)) => 2
         (count (datatable-paging 2 5 users))) => 5
         (:user (first (datatable-paging 2 5 users))) => "user3"
         (:user (last (datatable-paging 2 5 users))) => "user7"
  )

