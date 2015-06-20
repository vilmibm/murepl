(ns murepl.commands
  "This namespace is for the handling of \\ commands. It is transport
  independent. It returns string messages that can be sent to a user."
  (:require [schema.core :as s]
            [murepl.storage :refer [db Dbs]]
            [murepl.user :refer [User] :as u]))

;; new username password
;; login username password
;; change-password new-password
;; set-info <"key" "val">
;; help

;; TODO needs access to user stuff, presence atom
;; TODO schemafy

(s/defn dispatch [user :- User
                  command-str :- s/Str] :- s/Str
  nil)

(defn evaluate [code] nil)

(s/defn login [dbs :- Dbs
               presence
               user :- User
               password :- s/Str] :- s/Str
  ;; needs presence access
  nil)

(defn logout [presence user] nil)

(s/defn new [dbs :- Dbs
           username :- User
           password :- s/Str] :- s/Str
  nil)

(defn set-info [dbs username k v]
  nil)

(defn change-password [dbs username password]
  nil)

(defn help [db]
  nil)
