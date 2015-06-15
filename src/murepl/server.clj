(ns murepl.server
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as hk]))

;; todo
;; figure out how to handle special, non-eval commands like \help \new in a way that is consistent.
;; for each mode (http, telnet, ws), going to get a string. if it matches one of
;; the \ commands, do that. otherwise, run eval with appropriate user
;; authentication.

(def cfg {:port 7999})

(defn ws-handler [channel data]
  (hk/send! channel data))

(defn http-handler [data]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body    "SUP"})

;; TODO telnet handler

(defn handler [ring-request]
  (hk/with-channel ring-request channel
    (if (hk/websocket? channel)
      (hk/on-receive channel (partial ws-handler channel)))

    (hk/send! channel (http-handler data))))

(defn -main [& args]
  (log/infof "listening for http and websockets on port %s" (:port cfg))
  (hk/run-server handler (select-keys cfg [:port])))

