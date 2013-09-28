(ns murepl.core)

(declare ^:dynamic *world*)
(declare ^:dynamic *players*)

(defn init []
  (defonce ^:dynamic *world*   (ref {}))
  (defonce ^:dynamic *players* (ref #{})))

(defn add-player! [player-map]
  (dosync
   (alter *players* #(conj % player-map))
   (println *players*)))



;(defprotocol Player
;  [player]
;  (go [direction])
;  (look [])
;  (examine [object])
;  (say [something])
;  (yell [something])
;  (whisper [somebody something])
;  (take [object])
;  (inventory [])
;  (drop [])
;  (use [object action player])
;  (create-object [])
;  (create-room [])
;  (create-bot []))

;(defn foo
;  "I don't do a whole lot."
;  [x]
;   x "Hello, World!")
