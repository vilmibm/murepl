(defproject murepl "0.1.0-SNAPSHOT"
  :description "Multi User REPL"
  :url "http://murepl.clojurecup.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "0.3.3"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1889"]
                 [compojure "1.1.5"]
                 [himera "0.1.0-SNAPSHOT"]]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/js/main.js"
                                   :pretty-print true
                                   }}]})

