(ns murepl.testutils
  (:require [schema.core :as s]
            [murepl.storage :refer [Dbs]]
            [murepl.comms :refer [comm-service]]
            [murepl.server :refer [web-service]]
            [murepl.world :refer [world-service]]
            [murepl.testutils :refer [test-db reset-db!]]
            [murepl.async-service :refer [async-service]]
            [clojure.java.jdbc :as jdbc]))

(def services [comm-service web-service async-service world-service])

(def test-db {:subprotocol "postgresql"
              :subname "//localhost:5432/murepl_test"
              :user "murepl_test"
              :password "murepl_test"})

(def create-users-table-sql
  "CREATE TABLE users (name text primary key,
                       password text not null,
                       lastseen timestamp,
                       data json default '\"{}\"')")

(def create-meta-table-sql
  "CREATE TABLE meta (help text)")

(s/defn reset-db! [db :- Dbs]
  (let [tables ["users" "meta"]
        creates [create-users-table-sql create-meta-table-sql]]

    (doseq [table tables]
      (jdbc/execute! db [(str "DROP TABLE IF EXISTS " table)]))

    (doseq [create-sql creates]
      (jdbc/execute! db [create-sql]))))
