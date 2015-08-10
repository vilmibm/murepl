(ns murepl.namespaces
  (:require [schema.core :as s]
            [murepl.user :refer [User] :as u]))

(defn user->ns-name
  "TODO"
  [user]
  (str (:name user) "-ns"))

(s/defn user->ns :- clojure.lang.Symbol
  "TODO"
  [user :- User]
  (let [ns-sym (-> (user->ns-name user)
                   symbol)]
    (when-not (find-ns ns-sym)
      (create-ns ns-sym))
    ns-sym))
