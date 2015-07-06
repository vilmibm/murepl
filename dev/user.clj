(ns user
  (:require [clojure.pprint :as pprint]
            [clojure.tools.namespace.repl :refer [refresh]]
            [puppetlabs.trapperkeeper.app :as tka]
            [puppetlabs.trapperkeeper.bootstrap :as bootstrap]
            [puppetlabs.trapperkeeper.config :as config]
            [puppetlabs.trapperkeeper.core :as tk]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic system life cycle

(def system nil)

(defn init []
  (alter-var-root #'system
                  (fn [_] (tk/build-app
                            (bootstrap/parse-bootstrap-config! "./dev-resources/bootstrap.cfg")
                            (config/load-config "./dev-resources/config.conf"))))
  (alter-var-root #'system tka/init)
  (tka/check-for-errors! system))

(defn start []
  (alter-var-root #'system
                  (fn [s] (if s (tka/start s))))
  (tka/check-for-errors! system))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (tka/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Utilities for interacting with running system

(defn context
  "Get the current TK application context.  Accepts an optional array
  argument, which is treated as a sequence of keys to retrieve a nested
  subset of the map (a la `get-in`)."
  ([]
   (context []))
  ([keys]
   (get-in @(tka/app-context system) keys)))

(defn print-context
  "Pretty-print the current TK application context.  Accepts an optional
  array of keys (a la `get-in`) to print a nested subset of the context."
  ([]
   (print-context []))
  ([keys]
   (pprint/pprint (context keys))))
