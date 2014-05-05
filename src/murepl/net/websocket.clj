(ns murepl.net.websocket
  (:require [murepl.events :as events])
  (:import [org.webbitserver WebSocketHandler]))

(defonce clients (atom {}))

(defn ws-send [con msg]
  (.send con msg))

(defn open [con]
  (swap! clients assoc con nil)
  (println "Websocket connected: " con))

(defn close [con]
  (let [uuid (get @clients con)]
    (events/disconnect uuid))
  (swap! clients dissoc con)
  (println "Websocket disconnected: " con))

(defn register [con uuid]
  (events/connect uuid (partial ws-send con))
  (swap! clients assoc con uuid)
  (println "Websocket Player Registered: " uuid))

(def ws
  (proxy [WebSocketHandler] []
    (onOpen [c] (open c))
    (onClose [c] (close c))
    (onMessage [c m] (register c m))))
