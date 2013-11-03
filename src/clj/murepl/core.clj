(ns murepl.core
  (:require [murepl.common  :as common]
            [clojure.set    :as set]
            [clojure.string :as string])
  (:import [murepl.records MoveAction Room Player PlayerError]))

(declare world)
(declare players)
(declare rooms)
(declare passwords)

;; TODO handle this more cleanly
(declare place-player!)

(defn handle-move-action [action]
  (let [player (:player action)
        room   (:room action)]
    (place-player! player room)
    (:desc room)))

(defn execute-action
  "TODO"
  [action]
  (case action
    MoveAction (handle-move-action action)
    (PlayerError. "Unrecognized action type")))


(defn execute-actions 
  "Given a list of action records, handle them all. Returns an
homomorphic vector of messages to be returns by the API call that triggered these actions."
  [actions]
  (map execute-action actions))

(defn get-ro-rooms 
  "Return a copy of the rooms data structure"
  []
  @rooms)

(defn valid-auth? [auth player]
  (and (= (:name player) (:name auth))
       (= (:password player (:password auth)))))

(defn find-room-by-name [room-name] (get @rooms room-name))

(defn find-player [player-data] 
  (if-let [found-player (get @players (:uuid player-data))]
    (if (valid-auth? player-data found-player)
      found-player)))

(defn find-player-by-auth [name password] ;; TODO needs to stop seeking after a match.
  (let [auth {:name name :password password}]
    (second
          (first
           (filter #(valid-auth? auth (val %)) @players)))))

(defn find-player-by-uuid [uuid]
  (get @players uuid))

(defn lookup-location [player]
  (find-room-by-name (second (first (set/select #(= (:uuid player) (first %)) @world)))))

(defn players-in-room [room]
  (map find-player-by-uuid
       (map first
            (set/select #(= (:name room) (second %)) @world))))
(defn others-in-room [player room]
  (filter #(not (= (:uuid player) (:uuid %)))
          (players-in-room room)))

(defn duplicate-player-name? [player]
  (not (empty? (filter #(= (:name player) (:name (val %))) @players))))

(defn logout-player [player]
  (let [last-room (lookup-location player)
        observers (others-in-room player last-room)
        uuid      (:uuid player)]
    (println "LOGGING OUT" uuid)
    (dosync
     (alter world (fn [col] (set/select #(not= (first %) uuid) col))))
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
   (alter rooms #(assoc % (:name room) room))
   (doseq [[direction room-name] (seq (:exits room))]
     (let [exit-to     (find-room-by-name room-name)
           new-exits   (assoc (:exits exit-to) (common/opposite-dir direction) (:name room))
           new-exit-to (assoc exit-to :exits new-exits)]
       (alter rooms #(assoc % (:name exit-to) new-exit-to))))))

(defn place-player! [player room]
  (dosync
   (let [uuid      (:uuid player)
         room-name (:name room)]
     (alter world (fn [col] (set/select #(not (= (first %) uuid)) col)))
     (alter world #(conj % [uuid room-name])))))

(defn add-player! [player]
  (dosync
   (alter players #(assoc % (:uuid player) player)))
  (place-player! player (find-room-by-name "Lobby"))
  player)

(defn move-player! [direction player]
  (let [current-room (lookup-location player) 
        exit-to-name (get (:exits current-room) direction)]
    (if (nil? exit-to-name)
      nil ;; TODO throw
      (place-player! player (find-room-by-name exit-to-name)))))

(defn player-can-move? [player direction] 
  (contains? (:exits (lookup-location player)) direction))

(defn init! []
  (defonce world   (ref #{})) ;set of tuples that map uuid x room name
  (defonce players (ref {}))
  (defonce rooms   (ref {}))
  (defonce items   (ref {}))
  (defonce passwords (ref {}))
  (add-room! {:name "Lobby" :desc "A windowless room." :exits {}}))

(defn reset-game! []
  (dosync
   (ref-set world   #{})
   (ref-set players {})
   (ref-set rooms   {})
   (ref-set items   {})
   (ref-set passwords {})
  (init!)))
