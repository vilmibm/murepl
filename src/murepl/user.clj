(ns murepl.user
  (:require [schema.core :as s]
            [murepl.storage] ;; included for side effects, i'm sorry.
            [clojure.java.jdbc :as jdbc]))

(def db {:subprotocol "postgresql"
         :subname "//localhost:5432/murepl"
         :user "murepl"
         :password "murepl"})

(def User
  {:name s/Str
   :password s/Str
   :data (type {})})

(s/defn lookup [user :- User db]
  (first (jdbc/query db ["SELECT * FROM users WHERE name = ?" (:name user)])))

(s/defn new! [user :- User db] :- User
  "Given a user map, insert into db as a new user"
  (let [sql "INSERT INTO users VALUES (?, ?, ?)"]
    (jdbc/execute! db [sql (:name user) (:password user) (:data user)])
    user))

(s/defn update! [user :- User db] :- User
  "Given a user map, update it in the db"
  (let [sql "UPDATE users SET password=?, data=? WHERE name=?"]
    (jdbc/execute! db [sql (:password user) (:data user) (:name user)]))
  user)

(s/defn password [user :- User password :- s/Str] :- User
  "Given a user map, update its password"
  (assoc user :password password)
  user)

(s/defn data [user :- User data] :- User
  "Given a user map and new data, update its data"
  (assoc user :data data))
