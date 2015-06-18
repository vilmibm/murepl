(ns murepl.server
  (:require [clojure.tools.logging :as log]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [puppetlabs.trapperkeeper.services :refer [defservice get-service service-context]]
            [puppetlabs.comidi :refer [routes GET context routes->handler]]
            [org.httpkit.server :as hk]
            [murepl.async-service :as mas]))

;; todo
;; figure out how to handle special, non-eval commands like \help \new in a way that is consistent.
;; for each mode (http, telnet, ws), going to get a string. if it matches one of
;; the \ commands, do that. otherwise, run eval with appropriate user
;; authentication.

(def cfg {:port 7999})

(defn ws-handler [channel data]
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
      (println "HANDLING")

      (hk/with-channel req channel
        (println "IN CHANNEL")

        (if (hk/websocket? channel)
          (do
            (println "WS")
            (mas/user-join! async-svc "woo")
            (hk/on-receive channel (partial ws-handler channel))
            (hk/on-close channel (fn [_] (mas/user-part! async-svc "oow"))))

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
