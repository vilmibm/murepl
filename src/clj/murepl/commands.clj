(ns murepl.commands
  (:require [murepl.core :as core]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def directions [:north :south :west :east :up :down])

(defn go [direction]
  (if (some #{direction} directions)
    (fn [player]
      (let [player (core/find-player player)]
        (if (core/player-can-move? player direction)
          (core/move-player! direction player)
          nil) ;; TODO throw
        nil)))) ;; TODO throw

(defn create-room [name desc exit-map]
  (fn [_]
    (let [room   {:name name
                  :desc desc
                  :exit-map exit-map}
          result (core/add-room!)]
      {:result result :msg "You added a room."})))

(defn new-player [&{:keys [name password desc] :as player}]
  (fn [_]
    (let [player (assoc :uuid (uuid))
          result (core/add-player! player)]
      {:result result :msg "Congratulations, you exist."})))
