(defproject murepl "0.2.1"
  :description "Multi User REPL"
  :url "http://github.com/nathanielksmith/murepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main murepl.handler
  :ring {:handler murepl.handler/app}
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "0.3.3"]
            [lein-ring "0.8.3"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [com.taoensso/timbre "2.6.2"]
                 [amalloy/ring-gzip-middleware "0.1.2"]
                 [compojure "1.1.5"]

                 [aleph "0.3.2"]
                 [lamina "0.5.2"]

                 [ring/ring-json "0.2.0"]
                 [ring/ring-core "1.1.3"]
                 [ring/ring-jetty-adapter "1.1.3"]
                 [org.webbitserver/webbit "0.4.14"]])
