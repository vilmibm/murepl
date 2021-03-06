(ns murepl.core
  (:require [murepl.common     :as common]
            [clojure.set       :as set]
            [clojure.string    :as string]
            [taoensso.timbre   :as log]))

(declare ^:dynamic *world*)
(declare ^:dynamic *players*)
(declare ^:dynamic *rooms*)
(declare ^:dynamic *items*)

(defn log-command [player-data expr]
  (log/info (format "USER: %s COMMAND: %s" (:name player-data) expr))
  player-data)

(defn error-fn [e]
  (fn [_]
    {:error (str "I did not understand you. Please try again. Error was: "
                 (.getMessage e))}))

(defn with-player-fn [expr]
  (try
    (binding [*ns* (find-ns 'murepl.commands)]
      (eval expr))
    (catch Exception e (error-fn e))))

(defn eval-command [player expr] ((with-player-fn expr) player))

(defn eval-command2 [player-data expr]
  (try
    (binding [*ns* (find-ns 'murepl.commands)]
      (eval (flatten (list expr player-data))))
    (catch Exception e (error-fn e))))

(defn valid-auth? [auth player]
  (and (= (:name player) (:name auth))
       (= (:password player (:password auth)))))

(defn find-room-by-name [room-name] (get @*rooms* room-name))

(defn find-player [player-data]
  (if-let [found-player (get @*players* (:uuid player-data))]
    (if (valid-auth? player-data found-player)
      found-player)))

(defn find-player-by-auth [name password] ;; TODO needs to stop seeking after a match.
  (let [auth {:name name :password password}]
    (second
          (first
           (filter #(valid-auth? auth (val %)) @*players*)))))

(defn find-player-by-uuid [uuid]
  (get @*players* uuid))

(defn lookup-location [player]
  (find-room-by-name (second (first (set/select #(= (:uuid player) (first %)) @*world*)))))

(defn players-in-room [room]
  (map find-player-by-uuid
       (map first
            (set/select #(= (:name room) (second %)) @*world*))))
(defn others-in-room [player room]
  (filter #(not (= (:uuid player) (:uuid %)))
          (players-in-room room)))

(defn duplicate-player-name? [player]
  (not (empty? (filter #(= (:name player) (:name (val %))) @*players*))))

(defn logout-player [player]
  (let [last-room (lookup-location player)
        observers (others-in-room player last-room)
        uuid      (:uuid player)]
    (println "LOGGING OUT" uuid)
    (dosync
     (alter *world* (fn [col] (set/select #(not= (first %) uuid) col))))
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

(defn modify-player! [player]
  (dosync
   (alter *players* #(assoc % (:uuid player) player))))

(defn add-room! [room]
  (dosync
   (alter *rooms* #(assoc % (:name room) room))
   (doseq [[direction room-name] (seq (:exits room))]
     (let [exit-to     (find-room-by-name room-name)
           new-exits   (assoc (:exits exit-to) (common/opposite-dir direction) (:name room))
           new-exit-to (assoc exit-to :exits new-exits)]
       (alter *rooms* #(assoc % (:name exit-to) new-exit-to))))))

(defn place-player! [player room]
  (dosync
   (let [uuid      (:uuid player)
         room-name (:name room)]
     (alter *world* (fn [col] (set/select #(not (= (first %) uuid)) col)))
     (alter *world* #(conj % [uuid room-name])))))

(defn add-player! [player]
  (modify-player! player)
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
