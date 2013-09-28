(ns murepl.handler
  (:gen-class)
  (:use compojure.core
        ring.middleware.gzip
        ring.middleware.json)
  (:require [compojure.handler      :as handler]
            [compojure.route        :as route  ]
            [ring.server.standalone :as server ]
            [ring.util.response     :as resp   ]))

(defroutes api-routes
  (GET "/" [] (resp/file-response "index.html" {:root "resources/public"})))
       
(def app
  (-> (handler/site api-routes)
      (wrap-gzip)))

(defn -main [& args]
  server/serve app {:open-browser? false})
