(ns murepl.core
  (:require [murepl.common  :as common]
            [clojure.set    :as set]
            [clojure.string :as string]))

(declare ^:dynamic *world*)
(declare ^:dynamic *players*)
(declare ^:dynamic *rooms*)
(declare ^:dynamic *items*)

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
(defn find-player-by-uuid [uuid]
  (get @*players* uuid))
(defn lookup-location [player]
  (find-room-by-name (second (first (set/select #(= (:uuid player) (first %)) @*world*)))))

(defn others-in-room [player room]
  (println "o-i-r" player room)
  (filter #(not (= (:uuid player) (:uuid %)))
          (map find-player-by-uuid
               (map first
                    (set/select #(= (:name room) (second %)) @*world*)))))
(defn logout-player [player]
  (let [last-room (lookup-location player)
        observers (others-in-room player last-room)
        uuid      (:uuid player)]
    (dosync
     (alter *world* (fn [col] (set/select #(not= (first %) uuid) col)))
     (alter *players* #(dissoc % uuid)))
    observers))

(defn look-at [player room]
  (let [other-player-names (map :name (others-in-room player room))
        exit-names         (for [[k v] (:exits room)] (name k))]
    (string/join "\n" 
                 [(format "You find yourself in the %s: %s" (:name room) (:desc room))
                  (if (not (empty? other-player-names))
                    (format "Others here: %s" (string/join ", " other-player-names))
                    "You are alone here.")
                  (if (not (empty? exit-names))
                    (format "Exits: %s" (string/join ", " exit-names))
                    "There is no way out.")])))



(defn add-room! [room]
  (dosync
   (alter *rooms* #(assoc % (:name room) room))
   (doseq [[direction room-name] (seq (:exits room))]
     (let [exit-to     (find-room-by-name room-name)
           new-exits   (assoc (:exits exit-to) (common/opposite-dir direction) (:name room))
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
