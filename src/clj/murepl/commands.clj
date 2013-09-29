(ns murepl.commands
  (:require [murepl.core       :as core]
            [murepl.events     :as events]
            [clojure.data.json :as json]
            [clojure.string    :as string]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def directions [:north :south :west :east :up :down])

(defn say [msg]
  (fn [player-data]
    (if-let [player (core/find-player player-data)]
      (let [room   (core/lookup-location player)
            others (core/others-in-room player room)]
        (do
          (events/notify-players others (format "%s says: '%s'" (:name player) msg))
          {:result {} :msg (format "You say: '%s'" msg)})))))

(defn look []
  (fn [player-data]
    (if-let [player (core/find-player player-data)]
      {:result {} :msg (core/look-at (core/lookup-location player))}
      nil)))

(defn go [direction]
  (if (some #{direction} directions)
    (fn [player]
      (let [player (core/find-player player)]
        (if (core/player-can-move? player direction)
          (do
            (core/move-player! direction player)
            {:result {} :msg (core/look-at (core/lookup-location player))})
          nil) ;; TODO throw
        ))
    nil)) ;; TODO throw

(defn create-room [name desc exit-map]
  (fn [_]
    (let [room   {:name name
                  :desc desc
                  :exits exit-map}
          result (core/add-room! room)]
      {:result result :msg "You added a room."})))

(defn new-player [&{:keys [name password desc] :as player-data}]
  (fn [current-player]
    (if (not (nil? (core/find-player current-player)))
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
