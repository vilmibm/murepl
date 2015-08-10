(ns murepl.commands
  "This namespace is for the handling of / commands. It is transport
  independent. It returns string messages that can be sent to a user."
  (:require [schema.core :as s]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service]]
            [murepl.storage :refer [Dbs]]
            [murepl.sandbox :refer [sandbox]]
            [murepl.comms :as comm]
            [murepl.user :refer [User] :as u]))

;; new username password
;; login username password
;; change-password new-password
;; set-info <"key" "val">
;; help

;; TODO allow liberal whitespace in command strings

(s/defn login! :- s/Str
  [comm-svc :- s/Any ;; TODO
   db :- Dbs
   channel :- s/Any ;; TODO
   command-str]
   (let [[_ un pw] (re-matches #"\/login \"([^\"]+?)\" \"([^\"]+?)\"" command-str)
         db-user (if (nil? un) nil (u/lookup {:name un} db))]
     (cond

       ;; TODO user already logged in, reject

       (or (nil? un) (nil? pw))
       "try again with something like /login \"my rad username in double quotes\" \"my password in double quotes\""

       (nil? db-user)
       "no such user found :( check your username and/or password."

       (not= pw (:password db-user))
       "wrong password."

       :else
       (do
         (comm/register! comm-svc db-user channel)
         ;; TODO async to alert world to login
         (format "you are logged in as %s" un)))))

(s/defn logout! :- s/Str
  [comm-svc
   db :- Dbs
   user :- (s/maybe User)]
  (if (nil? user)
    "no active session."
    (if-let [logged-out (comm/unregister! comm-svc user)]
      "you are logged out."
      "logout failed. is there an active session?")))

;; TODO use meta table to store strings for all of the messages / default description / etc

(s/defn new-user! :- s/Str
  [db :- Dbs
   command-str :- s/Str]
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

(s/defn set-info! :- s/Str
  [db :- Dbs user :- User command-str :- s/Str]
  (let [[_ k v] (re-matches #"^\/set\s+\"([^\"]+?)\"\s+\"([^\"]*?)\"" command-str)]
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
      "try again with something like /set \"favorite color\" \"yellow\"")))

;; TODO handle nil user
(s/defn change-password! :- s/Str
  [db :- Dbs user :- User command-str :- s/Str]
  (let [[_ pw] (re-matches #"^\/change-password \"([^\"]+?)\"" command-str)]
    (if (some? pw)
      (do  (-> user
               (assoc :password pw)
               (u/update! db))
           "password successfully updated. there is no need to log back in, but use it next time.")
      "try again with something like /change-password \"my password in double quotes\"")))

(s/defn help [db :- Dbs]
  nil)

(defn slash-command?
  "TODO"
  [command-str]
  (= \/ (first command-str)))

;; comment we now have code we need to sandbox eval against the user's namespace.
;; can create a new, random-name namespace that stays associated with the user
;; over the course of the server's life. if server restarts, context is lost;
;; but seems easiest to start there and then attempt serialization later.

(s/defn sb-eval
  "TODO"
  [comm-svc
   db :- Dbs
   user :- User
   channel
   command-str :- s/Str]
  (let [sb (sandbox user)
        code (read-string command-str)]
    (try
      (str (sb code))
      (catch Exception e
        (str e)))))

(s/defn dispatch* :- s/Str
  [comm-svc ;; TODO
   db :- Dbs
   user :- (s/maybe User)
   channel ;; TODO
   command-str :- s/Str]
  (if-not (slash-command? command-str)
    (if (nil? user)
      "please log in."
      (sb-eval comm-svc db user channel command-str))
    (condp re-find command-str
      #"^\/help" (help db)
      #"^\/new " (new-user! db command-str)
      #"^\/change-password " (change-password! db user command-str)
      #"^\/(exit|logout|quit)" (logout! comm-svc db user)
      #"^\/login " (login! comm-svc db channel command-str)
      #"^\/set " (set-info! db user command-str)
      "oops, i didn't understand you. type /help for assistance.")))

(defprotocol CommandService
  (dispatch [this db channel command-str]))

(defservice command-service
  CommandService
  [CommService]
  (init [this ctx] ctx)
  (dispatch [this db channel command-str]
            (let [comm-svc (get-service this :CommService)
                  user (comm/channel->user comm-svc channel)]
              (dispatch* comm-svc db user channel command-str))))
