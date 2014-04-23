(ns murepl.routes
  (:require [[murepl.sandbox  :as sandbox]
             [murepl.core     :as core   ]

             [cheshire.core   :refer [generate-string parse-string]]
             [compojure.core  :refer [defroutes POST GET]          ]
             [compojure.route :refer [resources]                   ]])
  (:import
   (murepl.records PlayerError)))

(defn build-response [status type msg] {:status status
                                        :headers {"Content-Type" "application/json"}
                                        :body (json/generate-string {:type type
                                                                     :msg msg})}
(defn get-player-data [request]
  (if-let [data (-> (:headers request)
                    (get "player")
                    (parse-string true))]
    (core/find-player data)))

(def error-action? (partial instance? PlayerError)

(defroutes api-routes
  (GET "/" [] {:status 301 :headers {"Location" "/index.html"}})

  (resources "/")

  (POST "/eval" [expr :as r]
        (if-let [player (get-player-data r)]
          (let [actions (sandbox/eval-command player expr)]
            (if (errors? actions)
              (build-response 400 :command-error (concat ["Something(s) went wrong executing that command: "]
                                                         (map :msg (filter error-action? actions))))
              (build-response 200 :success (core/execute-actions actions))))
          (build-response 403 :player-error "No player data provided or no such player"))))
