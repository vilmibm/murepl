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
          result (core/add-room! room)]
      {:result result :msg "You added a room."})))

(defn new-player [&{:keys [name password desc] :as player-data}]
  (fn [player]
    ;; TODO why find-player sending new?
    (println "looking for" player)
    (println (core/find-player player))
    (if (not (nil? (core/find-player player)))
      {:result {} :msg "You already have an active player!"}
      (let [new-player (assoc player-data :uuid (uuid))
            result (core/add-player! new-player)]
        {:player (json/write-str result) :result result :msg "Congratulations, you exist."}))))

;; fixtures
(defn lucy []
  (new-player :name "lucy" :desc "someone" :password "foo"))
(defn joe []
  (new-player :name "joe" :desc "someone" :password "foo"))
(defn alley []
  (create-room "alley" "dark" {:south "Lobby"}))
