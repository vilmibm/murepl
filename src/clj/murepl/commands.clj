(ns murepl.commands
  (:require [murepl.core       :as core]
            [murepl.common     :as common]
            [murepl.events     :as events]
            [clojure.data.json :as json]
            [clojure.string    :as string]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

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
      {:result {} :msg (core/look-at player (core/lookup-location player))}
      nil)))

(defn go [direction]
  (if (common/valid-direction? direction)
    (fn [player]
      (let [player       (core/find-player player)
            player-name  (:name player)
            old-room     (core/lookup-location player)]
        (if (core/player-can-move? player direction)
          (do
            (core/move-player! direction player)
            (let [new-room        (core/lookup-location player)
                  leave-observers (core/others-in-room player old-room)
                  enter-observers (core/others-in-room player new-room)
                  came-from       (common/pretty-came-from (common/opposite-dir direction))]
              (events/notify-players leave-observers (format "%s leaves %s" player-name (name direction)))
              (events/notify-players enter-observers 
                                     (format "%s enters from %s" player-name came-from))
              {:result {} :msg (core/look-at player new-room)}))
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
