(ns murepl.commands-test
  (:require [murepl.commands :refer :all]
            [murepl.testutils :refer [test-db reset-db!]]
            [murepl.user :as u]
            [schema.test :refer [validate-schemas]]
            [clojure.test :refer :all]))

(use-fixtures :each (fn [f] (reset-db! test-db) (f)))
(use-fixtures :once validate-schemas)

(defmacro with-swap [called-atom fn-symbol & body]
  `(with-redefs [~fn-symbol (fn [& _#] (swap! ~called-atom (constantly true)))]
     (do ~@body)
     (swap! ~called-atom (constantly false))))

(def nate {:name "nate" :password "foo" :data {}})

(deftest dispatch-test
  (testing "when dispatching"
    (let [called (atom false)]
      (testing "and the command is"
        (testing "new it is called"
          (with-swap called murepl.commands/new-user!
            (dispatch test-db nil "/new pukey puke")
            (is @called)))
        (testing "login it is called"
          ;; TODO
          )
        (testing "change-password it is called"
          (with-swap called murepl.commands/change-password!
            (dispatch test-db nate "/change-password foobarbaz")
            (is @called)))
        (testing "set-info it is called"
          (with-swap called murepl.commands/set-info!
            (dispatch test-db nate "/set foobar baz")
            (is @called)))
        (testing "help it is called"
          (with-swap called murepl.commands/help
            (dispatch test-db nil "/help")
            (is @called)))))
    (testing "and the command is not found")))

(deftest new-user-test
  (testing "when creating a new user"

    (testing "and the user doesn't exist"
      (let [command-str "/new \"borges\" \"labyrinth\""
            result (dispatch test-db nil command-str)
            borges {:name "borges" :password "labyrinth" :data {}}
            db-result (u/lookup borges test-db)]
        (testing "get back message"
          (is (re-find #"created" result)))
        (testing "the user is created"
          (is (= "borges" (:name db-result)))
          (is (= "labyrinth" (:password db-result))))))

    (testing "but username already exists"
      (testing "an appropriate message is returned"
        (let [command-str "/new \"borges\" \"labyrinth\""
              result (dispatch test-db nil command-str)]
          (is (re-find #"already exists" result)))))

    (testing "but the command is malformed"
      (testing "an appropriate message is returned"
        (let [command-str "/new borges labyrinth"
              result (dispatch test-db nil command-str)]
          (is (re-find #"try again.*new" result)))))))

(deftest change-password-test
  (testing "when changing a user's password"
    (let [borges {:name "borges"
                  :password "labyrinth"}
          _ (u/new! borges test-db)]

      (testing "and command is not malformed"
        (let [command-str "/change-password \"aleph\""
              result (dispatch test-db borges command-str)
              db-result (u/lookup borges test-db)]
          (testing "it is updated"
            (= "aleph" (:password db-result)))
          (testing "appropriate message returned"
            (is (re-find #"updated" result)))))

      (testing "but the command is malformed"
        (let [command-str "/change-password foobarbaz"
              result (dispatch test-db borges command-str)]
          (testing "an appropriate message is returned"
            (is (re-find #"try again.*change-password" result))))))))

(deftest set-info-test

  )

(deftest login-test)
(deftest logout-test)
(deftest set-info-test)
(deftest help-test)

