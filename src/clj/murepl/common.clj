(ns murepl.common
  (:require [clojure.data.json :as json]))

(defn get-player-data [request]
  (let [raw (get (:headers request) "player")]
    (if (nil? raw)
      nil
      (into {}
            (for [[k v] (json/read-str raw)]
              [(keyword k) v])))))
