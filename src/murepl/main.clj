(ns murepl.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [murepl.server :as srv]
            [murepl.async :as async]))

(def cfg {:port 7999})

(defn -main [& args]
  (log/infof "listening for http and websockets on port %s" (:port cfg))
  (async/init!)
  (future (srv/run! cfg)))



