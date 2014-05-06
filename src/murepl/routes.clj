(ns murepl.routes
  (:require [cheshire.core   :as json]
            [compojure.core  :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [taoensso.timbre :as log]
            [murepl.commands] ; required for binding form
            [murepl.core     :as core]))

(defn build-response [body-map]
  (let [body-map (if (contains? body-map :player)
                   (assoc body-map :player (select-keys (:player body-map)
                                                        [:name :desc :uuid :password]))
                   body-map)]
    {:status 200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body (json/encode body-map)}))

(defn get-player-data [request]
  (if-let [raw (get (:headers request) "player")]
    (json/decode raw true)
    nil))

(defroutes api-routes
  (GET "/" [] {:status 301 :headers {"Location" "/index.html"}})

  (resources "/")

  (POST "/eval" [expr :as r]
        (log/debug "STRING EXPR: " expr)
        (let [expr (read-string expr)]
          (-> (get-player-data r)
              (core/log-command expr)
              (core/eval-command expr)
              (build-response)))))
