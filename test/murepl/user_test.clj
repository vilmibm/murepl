(ns murepl.user-test
  (:require [murepl.user :as u]
            [murepl.testutils :refer [test-db reset-db!]]
            [schema.test :refer [validate-schemas]]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]))

(use-fixtures :each (fn [f] (reset-db! test-db) (f)))
(use-fixtures :once validate-schemas)

(deftest user-crud
  (testing "when creating a user"
    (let [user {:name "hi" :password "yeah" :lastseen nil :data {:augen "blau"}}
          result (u/new! user test-db)]
      (testing "user is returned"
        (is (= user result)))
      (testing "user is created in db"
        (is (= user (u/lookup user test-db))))))
  (testing "when updating a user"
    (let [user {:name "there" :password "yeah" :lastseen nil :data {:color "purple"}}
          updated-user (assoc user :data {:color "black"})
          _ (u/new! user test-db)
          result (u/update! updated-user test-db)]
      (testing "user is returned"
        (is (= updated-user result)))
      (testing "user is updated in db"
        (is (= updated-user (u/lookup user test-db))))))
  (testing "when deleting a user"
    (comment not implemented yet.)))
