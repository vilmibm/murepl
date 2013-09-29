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

(defn ws-open [con]
  (println "connected" con)
  (swap! clients assoc con nil))

(defn ws-close [con]
  ;; TODO tell core to remove player
  (swap! clients dissoc con))

(defn ws-message [con uuid]
  (swap! clients assoc con uuid))

(def ws
  (proxy [WebSocketHandler] []
    (onOpen [c] (ws-open c))
    (onClose [c] (ws-close c))
    (onMessage [c m] (ws-message c m))))

(defn send-msg-to-player [player msg]
  (if-let [con (get (set/map-invert @clients) (:uuid player))]
    (println "Trying to send" con msg)
    (.send con (json/write-str msg))))
