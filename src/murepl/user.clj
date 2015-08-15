(ns murepl.user
  (:require [schema.core :as s]
            [murepl.storage :refer [db Dbs]]
            [clojure.java.jdbc :as jdbc]))

(def User
  {:name s/Str
   (s/optional-key  :password) s/Str
   ;; TODO figure out actual date/time type
   (s/optional-key :lastseen) (s/maybe s/Str)
   ;; TODO don't do this type thing
   :data (type {})})

(def SearchUser
  {:name s/Str
   (s/optional-key  :password) s/Str
   ;; TODO figure out actual date/time type
   (s/optional-key :lastseen) (s/maybe s/Str)
   (s/optional-key :data) (type {})})

(s/defn lookup [user :- SearchUser, db :- Dbs] :- User
  (first (jdbc/query db ["SELECT * FROM users WHERE name = ?" (:name user)])))

(s/defn new! [user :- User db :- Dbs] :- User
  "Given a user map, insert into db as a new user"
  (let [sql "INSERT INTO users VALUES (?, ?, DEFAULT, ?)"]
    (jdbc/execute! db [sql (:name user) (:password user) (:data user)])
    user))

(s/defn update! [user :- User db :- Dbs] :- User
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
