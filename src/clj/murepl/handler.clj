(ns murepl.handler
  (:gen-class)
  (:require murepl.commands
            [murepl.core                :as core]
            [murepl.events              :as events]

            [clojure.data.json          :as json]
            [clojure.tools.nrepl.server :as nrsrv]

            [taoensso.timbre            :as log]

            [ring.adapter.jetty         :refer (run-jetty)]
            [ring.middleware.clj-params :refer :all]
            [ring.middleware.gzip       :refer :all]
            [ring.util.response         :as resp]

            [compojure.core             :refer :all]
            [compojure.handler          :as handler]
            [compojure.route            :as route])
  (:import (org.webbitserver WebServer
                             WebServers
                             WebSocketHandler)
           (org.webbitserver.handler StaticFileHandler)))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})

(defn error-fn [e]
  (fn [_]
    {:error (str "I did not understand you. Please try again. Error was: " (.getMessage e))}))

(defn with-player-fn [expr]
  (try
    (binding [*ns* (find-ns 'murepl.commands)]
      (eval expr))
    (catch Exception e (error-fn e))))

(defn eval-command [player expr] ((with-player-fn expr) player))

(defn get-player-data [request]
  (let [raw (get (:headers request) "player")]
    (if (nil? raw)
      nil
      (into {}
            (for [[k v] (json/read-str raw)]
              [(keyword k) v])))))

(defn log-command [player expr]
  (do
    (log/info (format "USER: %s COMMAND: %s" (:name player) expr))
    player))

(defroutes api-routes
  ;; index page. serves up repl
  (POST "/eval" [expr :as r]
        (-> (get-player-data r)
            (log-command expr)
            (eval-command expr)
            (build-response)))
  (GET "/" [] {:status 301 :headers {"Location" "/index.html"}})

  (route/resources "/"))

(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))

(defn -main [& args]

  (let [args     (apply array-map args)
        host     (or (get args ":host") "localhost")
        port     (Integer. (or (get args ":port") 8888))
        ws-port  (Integer. (or (get args ":ws-port") 8889))
        log-file (or (get args ":log-file") "/tmp/MUREPL.log")]

    (log/set-config! [:timestamp-pattern] "yyyy-MM-dd HH:mm:ss ZZ")
    (log/set-config! [:appenders :spit :enabled?] true)
    (log/set-config! [:shared-appender-config :spit-filename] log-file)

    (log/debug "STARTUP: creating game world")
    (core/init!)

    (log/debug "STARTUP: starting nrepl")
    (defonce nrepl (nrsrv/start-server :port 7888))

    (log/debug "STARTUP: starting jetty on" host "port" port)
    (run-jetty app {:port port :host host :join? false})

    (log/debug "STARTUP: starting webbit on localhost port" ws-port)
    (doto (WebServers/createWebServer ws-port)
      (.add "/socket" events/ws)
      (.start))))
