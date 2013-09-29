(ns murepl.core)

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

(defn init! []
  (defonce ^:dynamic *world*   (ref #{})) ;set of tuples that map uuid x room name
  (defonce ^:dynamic *players* (ref {}))
  (defonce ^:dynamic *rooms*   (ref {}))
  (defonce ^:dynamic *items*   (ref {}))
  (add-room! {:name "Lobby" :desc "A windowless room." :exits {}}))

(defn reset! []
  (dosync
   (ref-set *world*   #{})
   (ref-set *players* {})
   (ref-set *rooms*   {})
   (ref-set *items*   {}))
  (init!))

(defn find-room   [room-name] (get @*rooms*   room-name))
(defn find-player [uuid]      (get @*players* uuid))
(defn lookup-location [uuid]
  (second (first (clojure.set/select #(= uuid (first %)) @*world*))))

(defn add-room! [room]
  (dosync
   (alter *rooms* #(assoc % (:name room) room))
   (doseq [[direction room-name] (seq (:exits room))]
     (let [exit-to     (find-room room-name)
           new-exits   (assoc (:exits exit-to) (opposite-dir direction) (:name room))
           new-exit-to (assoc exit-to :exits new-exits)]
       (alter *rooms* #(assoc % (:name exit-to) new-exit-to))))))

(defn place-player! [uuid room-name]
  (dosync
   (let [room (find-room room-name)]
     (alter *world* (fn [col] (filter #(not (= (first %) uuid)) col)))
     (alter *world* #(conj % [uuid room-name])))))

(defn add-player! [player]
  (dosync
   (alter *players* #(assoc % (:uuid player) player)))
  (place-player! (:uuid player) "Lobby"))

(defn move-player! [direction uuid]
  (let [current-room-name (lookup-location uuid)
        current-room      (find-room current-room-name)
        exit-to-name      (get (:exits current-room) direction)]
    (if (nil? exit-to-name)
      nil ;; TODO throw
      (place-player! uuid exit-to-name))))

(defn player-can-move? [auth direction] true) ; TODO make it real
