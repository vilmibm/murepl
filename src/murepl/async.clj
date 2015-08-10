(ns murepl.async
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [>! <! close! go chan alts!]]))

(defn init! []
  {:joins (chan)
   :parts (chan)
   :notifications (chan)})

(defn go! [channels]
  (go
    #_(while true
      (println (<! (:notifications channels)))))

    (go
      (while true
        (let [{:keys [joins parts notifications]} channels
              [v ch] (alts! [joins parts])]
          (condp = ch
            joins (>! notifications "user joined")
            parts (>! notifications "user parted"))))))

(defn destroy! [channels]
  ;; TODO figure out how to stop those go blocks?
  (doseq [chan (vals channels)]
    (close! chan)))

(defn user-join! [channels user]
  (go (>! (:joins channels) user)))

(defn user-part! [channels user]
  (go (>! (:parts channels) user)))
