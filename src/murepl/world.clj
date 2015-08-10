(ns murepl.world
  (:require [schema.core :as s]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service]]
            [murepl.user :refer [User]]))

(def Room {:name s/Str
           :exits {s/Keyword java.util.UUID}
           :description s/Str
           (s/optional-key :id) java.util.UUID})

;; TODO rooms will likely become a map of various state refs.

(s/defn create-room!* :- Room
  "TODO"
  [this
   state
   room :- Room]
  (comment generate id, add to rooms, return new room with id added)
  nil)

(s/defn place-user!* :- Room
  "TODO"
  [this
   state
   user :- User
   room :- Room]
  (comment update state stuff as needed, return room)
  room)

(s/defn users-in* :- [User]
  "TODO"
  [this
   state
   room :- Room]

  (comment figure out what users are in this room, return list of users)

  [])

(defprotocol WorldService
  "This service manages the state of the actual game world: what rooms exist,
  what items exist, what players are in what rooms, etc"
  (create-room! [this room])
  (place-user! [this user room])
  (users-in [this room]))

(defservice world-service
  WorldService
  [AsyncService]
  (init [this ctx]
        (comment make a ref for world state)
        (assoc ctx
               :async-svc (get-service this :AsyncService)))

  (create-room! [this room]
                (comment extract stuff from ctx / call *))

  (place-user! [this user room]
               (comment extract stuff from ctx / call *))

  (users-in [this room]
            (comment extract stuff from ctx / call *)))
