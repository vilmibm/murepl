(ns murepl.server
  (:require [clojure.tools.logging :as log]
            [murepl.async :as a]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [puppetlabs.comidi :refer [routes GET context routes->handler]]
            [org.httpkit.server :as hk]))

;; todo
;; figure out how to handle special, non-eval commands like \help \new in a way that is consistent.
;; for each mode (http, telnet, ws), going to get a string. if it matches one of
;; the \ commands, do that. otherwise, run eval with appropriate user
;; authentication.

(defn ws-handler [channel data]
  (println channel)
  (println "GOT WS" data)
  (hk/send! channel data))

;; TODO telnet handler

(defn static-routes []
  (-> (routes (GET "/" [_] (redirect "/index.html")))
      routes->handler
      (wrap-resource "public")
      wrap-content-type))

(defn handler [req]
  (let [http-handler (static-routes)]
    (println "HANDLING")

    (hk/with-channel req channel
      (println "IN CHANNEL")

      (if (hk/websocket? channel)
        (do
          (println "WS")
          (a/user-join! "woo")
          (hk/on-receive channel (partial ws-handler channel)))

        (hk/send! channel (http-handler req))))))

(defn run! [cfg]
  (hk/run-server handler (select-keys cfg [:port])))
