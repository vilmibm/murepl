(ns murepl.server
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service service-context]]
            [puppetlabs.comidi :refer [routes GET context routes->handler]]
            [org.httpkit.server :as hk]
            [murepl.commands :as cmd]
            [murepl.comms :as comm]
            [murepl.storage :as st]
            [murepl.async-service :as mas]))

(def cfg {:port 7999})

(defn ws-receive [db channel cmd-svc data]
  (log/infof "websocket message received: %s" data)

  (let [result (cmd/dispatch cmd-svc db channel data)]
    (hk/send! channel result)))

(defn ws-close [channel comm-svc _]
  (let [user (comm/channel->user comm-svc channel)]
    (log/infof "websocket closed, unregistering %s" (:name user))
    (comm/unregister! comm-svc user)))

;; TODO telnet handler

(defn static-routes []
  (-> (routes (GET "/" [_] (redirect "/index.html")))
      routes->handler
      (wrap-resource "public")
      wrap-content-type))

(defn handler [db async-svc cmd-svc comm-svc]
  (fn [req]
    (let [http-handler (static-routes)]

      (hk/with-channel req channel
        (if (hk/websocket? channel)
          (do
            (log/info "websocket connected")
            (hk/on-receive channel (partial ws-receive db channel cmd-svc))
            (hk/on-close channel (partial ws-close channel comm-svc)))

          (hk/send! channel (http-handler req)))))))

(defservice web-service
  [AsyncService CommService CommandService]
  (init [this ctx]
        (let [async-svc (get-service this :AsyncService)
              cmd-svc (get-service this :CommandService)
              comm-svc (get-service this :CommService)]
          (merge ctx {:handler (handler st/db async-svc cmd-svc comm-svc)})))

  (start [this ctx]
         (log/infof "listening for http and websockets on port %s" (:port cfg))
         (let [server (hk/run-server (:handler (service-context this)) (select-keys cfg [:port]))]
           (assoc ctx :web-server server)))

  (stop [this ctx]
        ((:web-server ctx))
        ctx))
