(ns murepl.commands-test
  (:require [murepl.commands :refer :all]
            [murepl.testutils :refer [test-db reset-db!]]
            [schema.test :refer [validate-schemas]]
            [clojure.test :refer :all]))

(use-fixtures :each (fn [f] (reset-db! test-db) (f)))
(use-fixtures :once validate-schemas)

(deftest dispatch-test
  (testing "when dispatching"
    (testing "and the command is"
      (testing "new it is called")
      (testing "login it is called")
      (testing "change-password it is called")
      (testing "set-info it is called")
      (testing "help it is called"))
    (testing "and the command is not found")))

(deftest new-test)
(deftest login-test)
(deftest set-info-test)
(deftest logout-test)
(deftest change-password-test)
(deftest set-info-test)
(deftest help-test)

