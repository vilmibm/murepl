(ns murepl.testutils
  (:require [schema.core :as s]
            [murepl.storage :refer [Dbs]]
            [clojure.java.jdbc :as jdbc]))

(def test-db {:subprotocol "postgresql"
              :subname "//localhost:5432/murepl_test"
              :user "murepl_test"
              :password "murepl_test"})

(def create-users-table-sql
  "CREATE TABLE users (name text primary key,
                       password text not null,
                       lastseen timestamp,
                       data json)")

(def create-meta-table-sql
  "CREATE TABLE meta (help text)")

(s/defn reset-db! [db :- Dbs]
  (let [tables ["users" "meta"]
        creates [create-users-table-sql create-meta-table-sql]]

    (doseq [table tables]
      (jdbc/execute! db [(str "DROP TABLE IF EXISTS " table)]))

    (doseq [create-sql creates]
      (jdbc/execute! db [create-sql]))))
