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
  (GET "/" [] (resp/file-response "index.html" {:root "resources/public"}))

  (POST "/eval" [expr :as r]
        (-> (get-player-data r)
            (eval-command expr)
            (build-response)))

  (route/resources "/")
  (route/not-found "four oh four"))

(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))

(defn -main [& args]
  (println "starting nrepl")
  (defonce nrepl (nrsrv/start-server :port 7888))
  (println "creating game world")
  (core/init!)
  (println "starting jetty")
  (run-jetty app {:port 9999 :host "0.0.0.0" :join? false})
  (println "starting webbit")
  (doto (WebServers/createWebServer 8888)
        (.add "/socket" events/ws)
        (.start)))
