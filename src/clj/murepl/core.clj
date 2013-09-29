(ns murepl.core
  (:require [clojure.set :as set]))

(declare ^:dynamic *world*)
(declare ^:dynamic *players*)
(declare ^:dynamic *rooms*)
(declare ^:dynamic *items*)

(defn opposite-dir [direction]
  (case direction
    :north :south
    :south :north
    :east :west
    :west :east
    :up :down
    :down :up))

(defn valid-auth? [auth player]
  (and (= (:name player) (:name auth))
       (= (:password player (:password auth)))))
(defn find-room-by-name [room-name] (get @*rooms* room-name))
(defn find-player [player-data] 
  (if-let [found-player (get @*players* (:uuid player-data))]
    (if (valid-auth? player-data found-player)
      found-player
      nil)
    nil))
(defn lookup-location [player]
  (find-room-by-name (second (first (set/select #(= (:uuid player) (first %)) @*world*)))))

(defn add-room! [room]
  (dosync
   (alter *rooms* #(assoc % (:name room) room))
   (doseq [[direction room-name] (seq (:exits room))]
     (let [exit-to     (find-room-by-name room-name)
           new-exits   (assoc (:exits exit-to) (opposite-dir direction) (:name room))
           new-exit-to (assoc exit-to :exits new-exits)]
       (alter *rooms* #(assoc % (:name exit-to) new-exit-to))))))

(defn place-player! [player room-name]
  (dosync
   (let [uuid (:uuid player)
         room (find-room-by-name room-name)]
     (alter *world* (fn [col] (set/select #(not (= (first %) uuid)) col)))
     (alter *world* #(conj % [uuid room-name])))))

(defn add-player! [player]
  (dosync
   (alter *players* #(assoc % (:uuid player) player)))
  (place-player! player "Lobby")
  player)

(defn move-player! [direction player]
  (let [current-room (lookup-location player) 
        exit-to-name (get (:exits current-room) direction)]
    (if (nil? exit-to-name)
      nil ;; TODO throw
      (place-player! player exit-to-name))))

(defn player-can-move? [player direction] 
  (contains? (:exits (lookup-location player)) direction))

(defn init! []
  (defonce ^:dynamic *world*   (ref #{})) ;set of tuples that map uuid x room name
  (defonce ^:dynamic *players* (ref {}))
  (defonce ^:dynamic *rooms*   (ref {}))
  (defonce ^:dynamic *items*   (ref {}))
  (add-room! {:name "Lobby" :desc "A windowless room." :exits {}}))

(defn reset-game! []
  (dosync
   (ref-set *world*   #{})
   (ref-set *players* {})
   (ref-set *rooms*   {})
   (ref-set *items*   {}))
  (init!))
