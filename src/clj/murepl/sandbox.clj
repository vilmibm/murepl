(ns murepl.sandbox
  (:require [clojail.core :refer [sandbox]                      ]
            [clojail.testers :refer [secure-tester-without-defs]])
  (:import
   (murepl.records PlayerError)))

(defn get-sandbox []
  (sandbox secure-tester-without-def :timeout 5000))

(defn raw-eval [expr]
  (binding [*ns* (find-ns 'murepl.commands)]
    (eval expr)))

;; (look) -> (look-cmd player rooms current-room)
(defn eval-command [player expr]
  (let [sb           (get-sandbox)
        rooms        (:rooms (core/get-state))
        current-room (core/lookup-location player)]
    (try
      (sb '((raw-eval expr) player rooms current-room))
      (catch Exception e
        [(PlayerError. "Command killed sandbox.")]))))
