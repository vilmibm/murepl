(ns murepl.handler
  (:use compojure.core
        ring.middleware.clj-params
        ring.middleware.gzip)
  (:require [compojure.handler     :as handler]
            [compojure.route       :as route  ]
            [ring.util.response    :as resp   ]))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})
  

(defn with-current-user [expr]
    {:msg (format "You do a thing: %s" (eval expr))})

(defroutes api-routes
  ;; index page. serves up repl
  (GET "/" [] (resp/file-response "index.html" {:root "resources/public"}))

  (POST "/eval" [expr]
        (-> (with-current-user expr)
            (build-response)))

  (route/resources "/")
  (route/not-found "four oh four"))
       
(def app
  (-> api-routes
      (wrap-clj-params)
      (wrap-gzip)))
