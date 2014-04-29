(ns murepl.net.telnet
  (:require [murepl.core  :as core]
            [murepl.events :as events]
            
            [clojure.data.json :as json]
            [clojure.string :as string]
            
            [aleph.tcp    :as tcp]
            [lamina.core  :as lamina]
            [gloss.core   :as gloss]))

(defonce clients (atom {}))

(defn telnet-send [ch msg]
  (if msg
    (lamina/enqueue ch msg)
    (lamina/enqueue ch "Something serious wrong bro.")))

(defn register-player [ch player-data]
  (let [client (get @clients ch)
        client-info (:client-info client)]
  (swap! clients assoc ch {:player-data player-data :client-info client-info})
  (events/connect (:uuid player-data) (partial telnet-send ch))
  (println "TCP/Telnet Player Registered: " (str player-data))))

(defn check-response [ch response]
  (if-let [player (:player response)]
    (let [player-data (json/read-str player :key-fn keyword)
          client (get @clients ch)]
      (if (and player (not (:player-data client)))
        (register-player ch player-data))))
  response)

(defn send-response [ch command-response]
  (if-let [msg (:msg command-response)]
    (telnet-send ch msg)
    (telnet-send ch (:error command-response))))

(defn eval-pipeline [ch expr]
  (let [client (get @clients ch)
        player-data (:player-data client)]
    (println "player-data: " (str player-data))
    (core/log-command player-data expr)
    (->> (read-string expr)
         (core/eval-command player-data)
         (check-response ch)
         (send-response ch))))

(defn close [ch]
  ;; Handles closed connections.
  (let [client (get @clients ch)
        player-data (:player-data client)
        client-info (:client-info client)]
    (swap! clients dissoc ch)
    (events/disconnect (:uuid player-data))
    (println "TCP/Telnet Socket disconnected: " client-info)))

(defn receive-message [ch message]
  (cond (= message "") #()
        (nil? message) (close ch)
        message (eval-pipeline ch message)))

(defn handler [ch client-info]
  ;; Handles new connections to the TCP server.
  (swap! clients assoc ch {:player-data nil :client-info client-info})
  (lamina/receive-all ch (partial receive-message ch))
  (println "TCP/Telnet Socket connected: " client-info)
  (lamina/enqueue ch "Weclome!"))

(defn start-server [port]
  (let [settings {:frame (gloss/string :utf-8 :delimiters ["\n" "\r\n"]) 
                  :port port}]
    (tcp/start-tcp-server handler settings)))
