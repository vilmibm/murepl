(ns murepl.events
  (:require [murepl.common      :refer :all]
            [murepl.core        :as core]
            [org.httpkit.server :refer :all]
            [clojure.set        :as set]
            [clojure.data.json  :as json])
  (:import (org.webbitserver WebServer
                             WebServers
                             WebSocketHandler)
           (org.webbitserver.handler StaticFileHandler)))

(defonce clients (atom {}))

(defn con-for-player [player]
  (get (set/map-invert @clients) (:uuid player)))

(defn send-msg-to-player [player msg]
  (if-let [con (con-for-player player)]
    (do
      (println "Trying to send" con msg)
      (.send con msg))))

(defn notify-players [players msg]
  (println players msg)
  (doseq [player players]
    (send-msg-to-player player msg)))

(defn ws-open [con]
  (println "connected" con)
  (swap! clients assoc con nil))

(defn ws-close [con]
  (let [player    (core/find-player-by-uuid (get @clients con))
        observers (core/logout-player player)]
    (notify-players observers (format "%s fades slowly away." (:name player))))
  (swap! clients dissoc con))

(defn ws-message [con uuid]
  (let [player    (core/find-player-by-uuid uuid)
        observers (core/others-in-room player (core/lookup-location player))]
  (swap! clients assoc con uuid)
  (notify-players observers (format "%s slowly fades into existence." (:name player)))))

(def ws
  (proxy [WebSocketHandler] []
    (onOpen [c] (ws-open c))
    (onClose [c] (ws-close c))
    (onMessage [c m] (ws-message c m))))
    
