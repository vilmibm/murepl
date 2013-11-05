(ns murepl.handler
  (:gen-class)
  (:require murepl.commands
            [murepl.common              :as common]
            [murepl.core                :as core]
            [murepl.events              :as events]

            [clojure.data.json          :as json]
            [clojure.tools.nrepl.server :as nrsrv]

            [clojail.core               :refer (sandbox)]
            [clojail.testers            :refer (secure-tester-without-def)]

            [taoensso.timbre            :as log]

            [ring.adapter.jetty         :refer (run-jetty)]
            [ring.middleware.clj-params :refer :all]
            [ring.middleware.gzip       :refer :all]
            [ring.util.response         :as resp]

            [compojure.core             :refer :all]
            [compojure.handler          :as handler]
            [compojure.route            :as route])
  (:import
   (murepl.records Player
                   PlayerError)

   (org.webbitserver WebServer
                     WebServers
                     WebSocketHandler)

   (org.webbitserver.handler StaticFileHandler)))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})

(defn build-error-map [e]
    {:error (str "I did not understand you. Please try again. Error was: " (.getMessage e))})

(defn error-fn [e]
  (fn [_]
    (build-error-map e)))

(defn eval-command [expr]
  (try
    (binding [*ns* (find-ns 'murepl.commands)]
      (eval expr))
    (catch Exception e (error-fn e))))

(defn get-sandbox []
  (sandbox secure-tester-without-def :timeout 5000))

(defn eval-command [player expr]
  (let [sb           (get-sandbox)
        rooms        (core/get-ro-rooms)
        current-room (core/lookup-location player)]
    ;; TODO try, catch here as well
    (try
      (sb '((eval-command expr) player rooms current-room))
      (catch Exception e [(build-error-map e)]))))

(defn get-player-data [request]
  ;; TODO validate player data against core
  (let [raw (get (:headers request) "player")]
    (if (nil? raw)
      nil
      (let [player-data (into {} (for [[k v] (json/read-str raw)]
                                   [(keyword k) v]))]
        (core/find-player player-data)))))

(defn log-command [player expr]
  (do
    (log/info (format "USER: %s COMMAND: %s" (:name player) expr))
    player))

(defn error-action?
  "Predicate for determining if a given action record is an error"
  [action]
  (instance? PlayerError action))

(defroutes api-routes
  ;; index page. serves up repl
  (POST "/eval" [expr :as r]
        (if-let [player (get-player-data r)]
                (do
                  (log-command player expr)
                  (let [actions (eval-command player expr)]
                     (if (some error-action? actions)
                       {:error (str (concat ["Something(s) went wrong executing that command: "]
                                            (map :msg (filter error-action? actions))))}
                       (build-response
                        (core/execute-actions actions))))))
                {:status 403})

  (POST "/login" [creds :as r]
        ;; TODO wtf
        (let [creds (:clj-params r)]
          (log/info (format "LOGIN FROM %s" creds))
          (if (core/valid-player? creds)
            {:status 200}
            {:status 403})))

  (POST "/create" [data :as r]
        ;; TODO wtf
        (let [data (:clj-params r)]
          (log/info (format "CREATE FROM %s" data))
          (if (core/duplicate-player-name? (:name data))
            {:status 400 :body (pr-str {:error "Name already taken."})}
            (let [new-player (Player. (common/uuid) (:name data) (:desc data))]
              (core/add-player! new-player (:password data))
              {:status 200}))))

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
