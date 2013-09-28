(ns murepl.core)


(defn system
  []
  {:world (ref {})
   :players (ref #{})}
   :server nil
  )

(defn start
  [{:keys [world players] :as system}]
  (init-world world)
)


(defprotocol Player
  [player]
  (go [direction])
  (look [])
  (examine [object])
  (say [something])
  (yell [something])
  (whisper [somebody something])
  (take [object])
  (inventory [])
  (drop [])
  (use [object action player])
  (create-object [])
  (create-room [])
  (create-bot []))

(defn foo
  "I don't do a whole lot."
  [x]
   x "Hello, World!")
