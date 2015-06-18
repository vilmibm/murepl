(defproject murepl "1.0.0-SNAPSHOT"
  :description "Framework for multi user repl driven applications."
  :url "http://github.com/nathanielksmith/murepl"
  :license {:name "GPL v3"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :main puppetlabs.trapperkeeper.main
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [prismatic/schema "0.4.3"]
                 [cheshire "5.5.0"]
                 [clojail "1.0.6"]
                 [http-kit "2.1.18"]
                 [ring "1.4.0-RC1"]
                 [puppetlabs/kitchensink "1.1.0"]
                 [puppetlabs/trapperkeeper "1.1.1"]
                 [puppetlabs/comidi "0.1.3"]])
