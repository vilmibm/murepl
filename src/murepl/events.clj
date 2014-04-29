(ns murepl.events
  (:require [murepl.common      :refer :all]
            [murepl.core        :as core]))

(defn send-msg [player msg]
  ;; Invokes player's send-function function.
  (if-let [send-function (:send-function player)] 
    (do
      (send-function msg))))

(defn notify-players [players msg]
  (println "Sending:" msg)
  (doseq [player players]
    (send-msg player msg)))

(defn connect [uuid function]
  "Subscribe player to the events framework by giving it a send function,
   probably associated with its network connection that will be used to pass
   event messages to it."
  (let [player (core/find-player-by-uuid uuid) 
        observers (core/others-in-room player (core/lookup-location player))] 
    (core/modify-player! (assoc player :send-function function))
    (notify-players observers (format "%s slowly fades into existence." (:name player)))))

(defn disconnect [uuid]
  "Unsubscribe player identified by uuid from the events framework."
  (let [player (core/find-player-by-uuid uuid) 
        observers (core/others-in-room player (core/lookup-location player))] 
    (core/modify-player! (dissoc player :send-function))
    (notify-players observers (format "%s fades slowly away." (:name player)))))

