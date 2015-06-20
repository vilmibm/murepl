(ns murepl.server
  (:require [clojure.tools.logging :as log]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service service-context]]
            [puppetlabs.comidi :refer [routes GET context routes->handler]]
            [org.httpkit.server :as hk]
            [murepl.commands :as cmd]
            [murepl.async-service :as mas]))

;; todo
;; figure out how to handle special, non-eval commands like \help \new in a way that is consistent.
;; for each mode (http, telnet, ws), going to get a string. if it matches one of
;; the \ commands, do that. otherwise, run eval with appropriate user
;; authentication.

(def cfg {:port 7999})

(defn ws-handler [channel data]
  (log/infof "websocket message received: %s" data)
  ;; TODO determine user context before calling dispatch
  (cmd/dispatch nil data)
  (hk/send! channel data))

;; TODO telnet handler

(defn static-routes []
  (-> (routes (GET "/" [_] (redirect "/index.html")))
      routes->handler
      (wrap-resource "public")
      wrap-content-type))

(defn handler [async-svc]
  (fn [req]
    (let [http-handler (static-routes)]

      (hk/with-channel req channel
        (println "IN CHANNEL")

        (if (hk/websocket? channel)
          (do
            (log/info "websocket connected")
            (hk/on-receive channel (partial ws-handler channel))
            (hk/on-close channel (fn [_] (log/info "websocket closed"))))

          (hk/send! channel (http-handler req)))))))

(defservice web-service
  [AsyncService]
  (init [this ctx]
        (let [async-svc (get-service this :AsyncService)]
          (merge ctx {:handler (handler async-svc)})))

  (start [this ctx]
         (log/infof "listening for http and websockets on port %s" (:port cfg))
         (let [server (hk/run-server (:handler (service-context this)) (select-keys cfg [:port]))]
           (assoc ctx :web-server server)))

  (stop [this ctx]
        ((:web-server ctx))
        ctx))
