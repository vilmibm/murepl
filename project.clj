(defproject murepl "1.0.0-SNAPSHOT"
  :description "Framework for multi user repl driven applications."
  :url "http://github.com/nathanielksmith/murepl"
  :license {:name "GPL v3"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :main murepl.server
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [clojail "1.0.6"]
                 [mvxcvi/clj-pgp "0.8.2"]
                 [http-kit "2.1.18"]
                 [puppetlabs/comidi "0.1.3"]])
