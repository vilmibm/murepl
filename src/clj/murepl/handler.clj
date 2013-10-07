(ns murepl.handler
  (:gen-class)
  (:use compojure.core
        ring.middleware.clj-params
        ring.middleware.gzip)
  (:require murepl.commands
            [murepl.core                :as core]
            [murepl.events              :as events]
            [clojure.data.json          :as json]
            [clojure.tools.nrepl.server :as nrsrv]
            [ring.adapter.jetty         :refer (run-jetty)]
            [compojure.handler          :as handler]
            [compojure.route            :as route]
            [ring.util.response         :as resp])
  (:import (org.webbitserver WebServer
                             WebServers
                             WebSocketHandler)
           (org.webbitserver.handler StaticFileHandler)))

(def ws-port 8889)

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

(defroutes api-routes
  ;; index page. serves up repl
  (POST "/eval" [expr :as r]
        (-> (get-player-data r)
            (eval-command expr)
            (build-response)))
  (GET "/" [] {:status 301 :headers {"Location" "/index.html"}})

  (route/resources "/"))

(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))

(defn -main [& args]
  (println "creating game world")
  (core/init!)

  (println "starting nrepl")
  (defonce nrepl (nrsrv/start-server :port 7888))

  (let [host (if (nil? (first args))  "localhost" (first args))
        port (if (nil? (second args)) 8888 (Integer. (second args)))]

    (println "starting jetty on" host "port" port)
    (run-jetty app {:port port :host host :join? false})

    (println "starting webbit on localhost port" ws-port)
    (doto (WebServers/createWebServer ws-port)
      (.add "/socket" events/ws)
      (.start))))
