(ns murepl.user-test
  (:require [murepl.user :as u]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]))

(def db {:subprotocol "postgresql"
         :subname "//localhost:5432/murepl_test"
         :user "murepl_test"
         :password "murepl_test"})

(def create-users-table-sql
  "CREATE TABLE users (name text, password text, data json)")

(use-fixtures :each (fn [f]
                      (jdbc/execute! db ["DROP TABLE IF EXISTS users"])
                      (jdbc/execute! db [create-users-table-sql])
                      (f)))

(deftest user-crud
  (testing "when creating a user"
    (let [user {:name "hi" :password "yeah" :data {:augen "blau"}}
          result (u/new! user db)]
      (testing "user is returned"
        (is (= user result)))
      (testing "user is created in db"
        (is (= user (u/lookup user db))))))
  (testing "when updating a user"
    (let [user {:name "there" :password "yeah" :data {:color "purple"}}
          updated-user (assoc user :data {:color "black"})
          _ (u/new! user db)
          result (u/update! updated-user db)]
      (testing "user is returned"
        (is (= updated-user result)))
      (testing "user is updated in db"
        (is (= updated-user (u/lookup user db))))))
  (testing "when deleting a user"
    (comment not implemented yet.)))
