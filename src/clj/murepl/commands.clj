(ns murepl.commands
  (:require [clojure.data.json :as json]
            [murepl.core :as core]))

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
    ;; TODO check if player exists; deny them this if they do
    (let [player (assoc player :uuid (uuid))
          result (core/add-player! player)]
      {:player (json/write-str result) :result result :msg "Congratulations, you exist."})))
