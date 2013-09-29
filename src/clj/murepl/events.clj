(ns murepl.events
  (:require [murepl.common      :refer :all]
            [murepl.core        :as core]
            [org.httpkit.server :refer :all]
            [clojure.data.json  :as json]))

(def clients (atom {}))

(defn ws
  [req]
  (with-channel req con
    (println req con)
    (swap! clients assoc con nil)
    (println con "connected")
    (on-receive con (fn [uuid]
                      (swap! clients assoc con uuid)))
    (on-close con (fn [status]
                    (swap! clients dissoc con)))))

(defn ws'
  [req]
  (let [player-data (get-player-data req)]
    (if-let [player (core/find-player player-data)]
      (with-channel req con
        (swap! clients assoc (:uuid player) con)
        (println con "connected" player)
        (on-close con (fn [status]
                        (swap! clients dissoc (:uuid player))
                        (println con "disconnected. status:" status))))
      {:status 404})))

(defn send-msg-to-player [player msg]
  (if-let [con (get @clients (:uuid))]
    (send! con (json/write-str msg)
           false)))
