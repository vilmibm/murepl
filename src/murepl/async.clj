(ns murepl.async
  (:require [clojure.core.async :refer [>! <! close! go chan]]))

;; i want a thing that prints when a user joins and when a user leaves.

(defonce !state (atom nil))

;; TODO rename user-events to be joining specific
;; TODO add notifications bus
(defn user-join! [user]
  (go (>! (:user-events @!state) user)))

(defn init! []
  (println "INITTING")
  (reset! !state {:user-events (chan)})
  ;; listen for users
  (go
    (println "LISTENING")
    (while true
      (println "BLOCKING")
      (let [user (<! (:user-events @!state))]
        (println "USER JOINED " user)))))

(defn destroy! []
  (close! (:user-events @!state))
  (reset! !state nil))

