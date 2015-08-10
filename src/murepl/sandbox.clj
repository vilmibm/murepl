(ns murepl.sandbox
  (:require [clojail.core :as clojail]
            [clojail.testers :as tst]
            [murepl.namespaces :refer [user->ns]]))

(defn sandbox [user]
  (let [tester [(tst/blacklist-objects [clojure.lang.Compiler clojure.lang.Ref clojure.lang.Reflector
                                        clojure.lang.Namespace clojure.lang.Var clojure.lang.RT
                                        java.io.ObjectInputStream])
                (tst/blacklist-packages ["java.lang.reflect"
                                         "java.security"
                                         "java.util.concurrent"
                                         "java.awt"])
                (tst/blacklist-symbols
                 '#{alter-var-root intern eval catch
                    load-string load-reader addMethod ns-resolve resolve find-var
                    *read-eval* ns-publics ns-unmap set! ns-map ns-interns the-ns
                    push-thread-bindings pop-thread-bindings future-call agent send
                    send-off pmap pcalls pvals in-ns System/out System/in System/err
                    with-redefs-fn Class/forName})
                (tst/blacklist-nses '[clojure.main
                                      murepl.user
                                      murepl.server
                                      ;; TODO add as needed
                                      ])
                (tst/blanket "clojail")
                (tst/blanket "clj-pgp")]]
    (clojail/sandbox tester :namespace (user->ns user))))
