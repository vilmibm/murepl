(defproject murepl "0.1.0-SNAPSHOT"
  :description "Multi User REPL"
  :url "http://murepl.clojurecup.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :main murepl.handler
  :ring {:handler murepl.handler/app}
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "0.3.3"]
            [lein-ring "0.8.3"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1889"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [amalloy/ring-gzip-middleware "0.1.2"]
                 [compojure "1.1.5"]
                 [ring-clj-params "0.1.0"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-core "1.1.3"]
                 [ring/ring-jetty-adapter "1.1.3"]]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-dir "resources/public/js/"
                                   :output-to "resources/public/js/murepl.js"
                                   :pretty-print true
                                   }}]})

