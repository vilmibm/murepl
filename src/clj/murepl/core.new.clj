(ns murepl.core
  (:require [clojure.set :as set])
  (:import [murepl.records MoveAction Room Player PlayerError]))

(declare rooms)
(declare players)
(declare passwords)
(declare world)

(defn get-state [] {:rooms     @rooms
                    :players   @players
                    :passwords @passwords
                    :world     @world})

(defn lookup-location [player]
  "TODO")

(defn init! []
  "TODO")
