(ns murepl.commands
  "This namespace is for the handling of / commands. It is transport
  independent. It returns string messages that can be sent to a user."
  (:require [schema.core :as s]
            [murepl.storage :refer [Dbs]]
            [murepl.user :refer [User] :as u]))

;; new username password
;; login username password
;; change-password new-password
;; set-info <"key" "val">
;; help

;; TODO allow liberal whitespace in command strings

(defn evaluate [code] nil)

(s/defn login [db :- Dbs
               presence
               user :- User
               password :- s/Str] :- s/Str
  nil)

(defn logout [presence user] nil)

;; TODO use meta table to store strings for all of the messages / default description / etc

(s/defn new-user! [db :- Dbs command-str :- s/Str] :- s/Str
  (let [[_ un pw] (re-matches #"\/new \"([^\"]+?)\" \"([^\"]+?)\"" command-str)]
    (if (or (nil? un) (nil? pw))
      "try again with something like /new \"my rad username in double quotes\" \"my password in double quotes\""
      (if (some? (u/lookup {:name un} db))
        "sorry! a user with that name already exists :/"
        (do
          (u/new! {:name un
                   :password pw
                   :data {"description" "Little more than an etheral mist."}} db)
          "created! TODO")))))

(s/defn set-info!
  [db :- Dbs user :- User command-str :- s/Str] :- s/Str
  (let [[_ k v] (re-matches #"^\/set-info\s+\"([^\"]+?)\"\s+\"([^\"]*?)\"" command-str)]
    (cond

      ;; Remove a key
      (and (some? k) (empty? v))
      (do
        (-> user
            (assoc :data (dissoc (:data user) k))
            (u/update! db))
        (format "removed %s" k))

      ;; Add or change a key
      (and (some? k) (some? v))
      (do
        (-> user
            (assoc-in [:data k] v)
            (u/update! db))
        (format  "updated! %s set to %s" k v))

      ;; Malformed
      :else
      "try again with something like /set-info \"favorite color\" \"yellow\"")))

(s/defn change-password!
  [db :- Dbs user :- User command-str :- s/Str] :- s/Str
  (let [[_ pw] (re-matches #"^\/change-password \"([^\"]+?)\"" command-str)]
    (if (some? pw)
      (do  (-> user
               (assoc :password pw)
               (u/update! db))
           "password successfully updated. there is no need to log back in, but use it next time.")
      "try again with something like /change-password \"my password in double quotes\"")))

(s/defn help [db :- Dbs]
  nil)

(s/defn ^:always-validate dispatch
  [db :- Dbs user :- (s/maybe User) command-str :- s/Str] :- s/Str
  (condp re-find command-str
    #"^\/h" (help db)
    #"^\/new" (new-user! db command-str)
    #"^\/change-password" (change-password! db user command-str)
    #"^\/set" (set-info! db user command-str)))
