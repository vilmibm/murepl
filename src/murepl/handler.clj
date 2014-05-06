(ns murepl.handler
  (:gen-class)
  (:require [clojure.tools.nrepl.server :as nrsrv]
            [ring.middleware.params     :refer [wrap-params]]
            [ring.adapter.jetty         :refer (run-jetty)]
            [taoensso.timbre            :as log]
            [murepl.core                :as core]
            [murepl.routes              :refer [api-routes]]
            [murepl.net.telnet          :as telnet]
            [murepl.net.websocket       :as ws])
  (:import (org.webbitserver WebServers)))

(defn app [] (wrap-params api-routes))

(defn -main [& args]
  (let [args         (apply array-map args)
        host         (or (get args ":host") "localhost")
        port         (Integer. (or (get args ":port") 8888))
        ws-port      (Integer. (or (get args ":ws-port") 8889))
        telnet-port  (Integer. (or (get args ":telnet-port") 8891))
        log-file     (or (get args ":log-file") "/tmp/MUREPL.log")]

    (log/set-config! [:timestamp-pattern] "yyyy-MM-dd HH:mm:ss ZZ")
    (log/set-config! [:appenders :spit :enabled?] true)
    (log/set-config! [:shared-appender-config :spit-filename] log-file)

    (log/debug "STARTUP: creating game world")
    (core/init!)

    (log/debug "STARTUP: starting nrepl")
    (defonce nrepl (nrsrv/start-server :port 7888))

    (log/debug "STARTUP: starting telnet server on " host " port " telnet-port)
    (telnet/start-server telnet-port)

    (log/debug "STARTUP: starting jetty on" host "port" port)
    (run-jetty (app) {:port port :host host :join? false})

    (log/debug "STARTUP: starting webbit on localhost port" ws-port)
    (doto (WebServers/createWebServer ws-port)
      (.add "/socket" ws/ws)
      (.start))))
