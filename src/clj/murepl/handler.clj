(ns murepl.handler
  (:gen-class)
  (:use compojure.core
        ring.middleware.clj-params
        ring.middleware.gzip)
  (:require murepl.commands
            [murepl.core                :as core]
            [murepl.events              :as events]
            [murepl.common              :as common]
            [clojure.data.json          :as json]
            [clojure.tools.nrepl.server :as nrsrv]
            [ring.adapter.jetty         :refer (run-jetty)]
            [compojure.handler          :as handler]
            [compojure.route            :as route]
            [org.httpkit.server         :as httpkit]
            [ring.util.response         :as resp]))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})

(defn eval-command [player expr]
  (binding [*ns* (find-ns 'murepl.commands)]
    ;; TODO try/catch around eval
    (let [command-fn (binding [*ns* (find-ns 'murepl.commands)] (eval expr))]
      (println "Handling command from player" player)
      (let [result (command-fn player)]
        result))))

(defroutes api-routes
  ;; index page. serves up repl
  (GET "/" [] (resp/file-response "index.html" {:root "resources/public"}))
  (GET "/socket" [] events/ws)

  (POST "/eval" [expr :as r]
        (-> (common/get-player-data r)
            (eval-command expr)
            (build-response)))

  (route/resources "/")
  (route/not-found "four oh four"))

(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))

(defn -main [& args]
  (defonce nrepl (nrsrv/start-server :port 7888))
  (core/init!)
  (run-jetty app {:port 9999 :host "0.0.0.0"}))
