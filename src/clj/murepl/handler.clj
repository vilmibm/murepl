(ns murepl.handler
  (:use compojure.core
        ring.middleware.gzip
        ring.middleware.json)
  (:require [compojure.handler      :as handler]
            [compojure.route        :as route  ]
            [ring.util.response     :as resp   ]))

(defroutes api-routes
  ;; index page. serves up repl
  (GET "/" [] (resp/file-response "index.html" {:root "resources/public"}))

  ;; API routes

  ;; Rooms
  (POST "/api/v1/move/:direction" [direction] 200)
  (GET  "/api/v1/look/" [] 200)

  ;; Object Interaction
  (POST "/api/v1/take/:obj-id" [obj-id] 200)
  (POST "/api/v1/drop/:obj-id" [obj-id] 200)
  (GET  "/api/v1/inventory" [] 200)
  (POST "/api/v1/use/:obj-id/:verb/:verb-arg" [obj-id verb verb-arg] 200)
  (GET  "/api/v1/examine/:obj-id" [obj-id] 200)

  ;; Communication
  (POST "/api/v1/say" [] 200)
  (POST "/api/v1/yell" [] 200)
  (POST "/api/v1/whisper" [] 200)

  ;; Scripting
  (POST "/create-object" [] 200)
  (POST "/create-room" [] 200)
  (POST "/create-bot" [] 200)

  (route/resources "/")
  (route/not-found "four oh four"))
       
(def app
  (-> (handler/site api-routes)
      (wrap-gzip)))
