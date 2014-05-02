(ns murepl.routes
  (:require [cheshire.core   :as json]
            [compojure.core  :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [taoensso.timbre :as log]
            [murepl.commands] ; required for binding form
            [murepl.core     :as core]))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})

(defn get-player-data [request]
  (if-let [raw (get (:headers request) "player")]
    (json/parse-string raw true)
    nil))

(defn log-command [player expr]
  (do
    (log/info (format "USER: %s COMMAND: %s" (:name player) expr))
    player))

(defroutes api-routes
  (GET "/" [] {:status 301 :headers {"Location" "/index.html"}})

  (resources "/")

  (POST "/eval" [expr :as r]
        {:status 200
         :headers {"Content-Type" "application/json; charset=utf-8"}
         :body (json/encode {:msg "HI" :error "FOO"})}))
        ;(-> (get-player-data r)
        ;    (core/log-command expr)
        ;    (core/eval-command expr)
        ;    (build-response))))
