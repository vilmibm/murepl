(ns murepl.storage
  "http://hiim.tv/clojure/2014/05/15/clojure-postgres-json/"
  (:require [schema.core :as s]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :as json])
  (:import org.postgresql.util.PGobject))

(def Dbs {:subprotocol (s/eq "postgresql")
          :subname s/Str
          :user s/Str
          :password s/Str})

(def db {:subprotocol "postgresql"
         :subname "//localhost:5432/murepl"
         :user "murepl"
         :password "murepl"})

(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "json")
      (.setValue (json/encode value))))

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value)))

(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (json/parse-string value)
        :else value))))
