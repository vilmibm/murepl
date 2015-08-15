(ns murepl.world
  (:require [schema.core :as s]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service service-context]]
            [murepl.user :refer [User]]))

(def Room {:name s/Str
           :exits {s/Keyword java.util.UUID}
           :description s/Str
           (s/optional-key :id) java.util.UUID})

;; TODO rooms will likely become a map of various state refs.
;; TODO so; user data may have been updated in DB. Move to just storing user
;; names and then hydrating them (but where?)

(s/defn create-room!* :- Room
  "TODO"
  [{:keys [rooms room->users]}
   room :- Room]
  (let [id (java.util.UUID/randomUUID)
        new-room (assoc room :id id)]
    (dosync
     (alter rooms assoc id new-room)
     (alter room->users assoc id []))
    new-room))

(s/defn place-user!* :- Room
  "TODO"
  [{:keys [room->users user->room]}
   user :- User
   room :- Room]
  (let [old-user-room (@user->room (:name user))
        user!= (fn [u]
                 (println "COMPARING " (:name user) (:name u))
                 (not= (:name user) (:name u)))
        forget-user (partial filter user!=)
        remember-user #(conj % user)]
    (dosync
     (alter user->room assoc (:name user) (:id room))
     (alter room->users update (:id old-user-room) forget-user)
     (alter room->users update (:id room) remember-user))
    room))

(s/defn users-in* :- [User]
  "TODO"
  [{:keys [room->users]}
   room :- Room]
  (@room->users (:id room)))

(defprotocol WorldService
  "This service manages the state of the actual game world: what rooms exist,
  what items exist, what players are in what rooms, etc"
  (create-room! [this room])
  (place-user! [this user room])
  (users-in [this room]))

;; TODO periodic, background thread serialization of rooms DS
(defservice world-service
  WorldService
  [AsyncService]
  (init [this ctx]
        (assoc ctx
               ;; TODO more refs?
               :user->room (ref {})
               :rooms (ref {})
               :room->users (ref {})
               :async-svc (get-service this :AsyncService)))

  (create-room! [this room]
                (let [state (service-context this)]
                  (create-room!* state room)))

  (place-user! [this user room]
               (let [state (service-context this)]
                 (place-user!* state user room)))

  (users-in [this room]
            (let [state (service-context this)]
              (users-in* state room))))
