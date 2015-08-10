(defproject murepl "1.0.0-SNAPSHOT"
  :pedantic? :abort
  :description "Framework for multi user repl driven applications."
  :url "http://github.com/nathanielksmith/murepl"
  :license {:name "GPL v3"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :main puppetlabs.trapperkeeper.main
  :min-lein-version "2.0.0"
  :repl-options {:init-ns user}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[puppetlabs/trapperkeeper "1.1.1" :classifier "test"]
                                  [puppetlabs/kitchensink "1.1.0" :classifier "test"]]}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [stylefruits/gniazdo "0.4.0"]
                 [prismatic/schema "0.4.3"]
                 [cheshire "5.5.0"]
                 [clojail "1.0.6"]
                 [http-kit "2.1.18"]
                 [ring "1.4.0" :exclusions [joda-time org.clojure/tools.reader clj-time]]
                 [puppetlabs/kitchensink "1.1.0"]
                 [puppetlabs/trapperkeeper "1.1.1"]
                 [puppetlabs/comidi "0.1.3" :exclusions [org.clojure/tools.macro clj-time]]])
