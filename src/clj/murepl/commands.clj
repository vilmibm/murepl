(ns murepl.commands
  (:require [murepl.records    :refer :all]
            [murepl.rocore     :as rocore]
            [murepl.core       :as core]
            [murepl.common     :as common]
            [murepl.events     :as events]
            [clojure.data.json :as json]
            [clojure.string    :as string])
  (:import [murepl.records MoveAction PlayerError]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def no-such-user "Have you run connect or new-player yet?")

(defn that-way [rooms room direction]
  (rocore/lookup-room-by-id rooms (direction (:exits room))))

(defmacro defcmd [name argv body]
  `(defn ~name ~argv
     (fn ~(into [] (map symbol ["player" "rooms" "current-room"]))
       ~body)))

(defcmd go [direction]
  (if-let [next-room (that-way rooms current-room direction)]
    [(MoveAction. player next-room)]
    (PlayerError. "There is nothing in that direction.")))

(defcmd go-until-deadend [direction]
  (loop [room current-room actions []]
    (if (nil? room)
      actions
      (let [action ((go direction) player rooms room)]
        (if (instance? PlayerError action)
          (recur nil actions)
          (recur (:room action) (conj actions action)))))))

(defn connect [name password]
  (fn [_ _ _]
    (if-let [player (core/find-player-by-auth name password)]
      (do
        (println "FOUND PLAYER" player)
        (core/place-player! player (core/find-room-by-name "Lobby"))
        {:player (json/write-str player) :msg "Welcome back."})
      {:error "Sorry, no such player exists."})))

;;; NEW ^






(defn say [msg]
  (fn [player-data]
    (if-let [player (core/find-player player-data)]
      (let [room   (core/lookup-location player)
            others (core/others-in-room player room)]
        (do
          (events/notify-players others (format "%s says: '%s'" (:name player) msg))
          {:result {} :msg (format "You say: '%s'" msg)}))
      {:error no-such-user})))

(defn examine [player-name]
  (fn [player-data]
    (if-let [player (core/find-player player-data)]
      (let [player-name (if (= player-name "self") (:name player) player-name)
            room        (core/lookup-location player)
            players     (core/players-in-room room)
            looking-at  (filter #(= (:name %) player-name) players)]
        (println player-name room players looking-at)
        (if (empty? looking-at)
          {:result {} :msg "There is no one near you by that name."}
          {:result {} :msg (format "You examine %s: %s" player-name (:desc (first looking-at)))}))
      {:error no-such-user})))


(defn look []
  (fn [player-data]
    (if-let [player (core/find-player player-data)]
      {:result {} :msg (core/look-at player (core/lookup-location player))}
      {:error no-such-user})))

(defn go' [direction]
    (fn [player]
      (if (not (common/valid-direction? direction))
        {:error "Sorry, that's not a valid direction"}
        (if-let [player (core/find-player player)]
          (let [player-name  (:name player)
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
            {:error "You can't go that way."}))
          {:error no-such-user}))))

(defn create-room [name desc exit-map]
  (fn [player]
    (if-let [player (core/find-player player)]
      (let [room-data   {:name name
                         :desc desc
                         :exits exit-map}]
        (if (core/find-room-by-name name)
          {:error "Sorry, a room with that name already exists."}
          (let [result (core/add-room! room-data)]
            {:result result :msg "You added a room."})))
      {:error no-such-user})))

(defn new-player [name password desc]
  (fn [current-player]
    (let [player-data {:name name :password password :desc desc}]
          (if (not (nil? (core/find-player current-player)))
            {:error "You already have an active player!"}
            (if (core/duplicate-player-name? player-data)
              {:error "Sorry, a player already exists with that name"}
              (let [new-player (assoc player-data :uuid (uuid))
                    result (core/add-player! new-player)]
                {:player (json/write-str result) :result result :msg "Congratulations, you exist."}))))))

;; fixtures
(defn lucy []
  (new-player "lucy" "foo" "someone"))
(defn joe []
  (new-player "joe" "foo" "someone else"))
(defn alley []
  (create-room "alley" "dark" {:south "Lobby"}))
