(ns murepl.world-service-test
  (:require [clojure.test :refer :all]
            [puppetlabs.trapperkeeper.bootstrap :refer [with-app-with-config]]
            [puppetlabs.trapperkeeper.app :as tka]
            [murepl.testutils :refer [test-db reset-db!]]
            [schema.test :refer [validate-schemas]]
            [murepl.world :as w]))

(use-fixtures :once validate-schemas)
(use-fixtures :each (fn [f] (reset-db! test-db) (f)))


(def borges {:name "borges" :password "labyrinth" :data {}})
(def kafka {:name "kafka" :password "castle" :data {}})
