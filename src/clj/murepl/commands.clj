(ns murepl.commands
  (:require [murepl.core :as core]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

{:north "a dim hallway"
 :south "a tunnel"}

(defn create-room [name desc exit-map]
  (-> {:name name
       :desc desc
       :exit-map exit-map
       :contents #{}}
      (core/add-room!)))


(defn new-player [&{:keys [name password desc] :as player-map}]
  (-> player-map
      (assoc :uuid (uuid))
      (core/add-player!)))

