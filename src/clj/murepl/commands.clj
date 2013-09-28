(ns murepl.commands
  (:require [murepl.core :as core]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn new-player [&{:keys [name password desc] :as player-map}]
  (-> player-map
      (assoc :uuid (uuid))
      (core/add-player!)))

