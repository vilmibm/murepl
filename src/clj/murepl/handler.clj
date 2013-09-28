(ns murepl.handler
  (:gen-class)
  (:use compojure.core
        ring.middleware.clj-params
        ring.middleware.gzip)
  (:require murepl.commands
            [murepl.core        :as core]
            [ring.adapter.jetty :refer (run-jetty)]
            [compojure.handler  :as handler]
            [compojure.route    :as route  ]
            [ring.util.response :as resp   ]))

(defn build-response [body-map]
    {:status 200
     :headers {"Content-Type" "application/clojure; charset=utf-8"}
     :body (pr-str body-map)})

(defn foo [x] x)

(defn eval-command [expr]
  (binding [*ns* (find-ns 'murepl.commands)] (eval expr)))

(defn with-current-user [expr]
  (let [result (eval-command expr)]
    {:msg (format "You do a thing: %s" result)}))

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

(defn -main [& args]
  (core/init)
  (run-jetty app {:port 9999 :host "0.0.0.0"}))
