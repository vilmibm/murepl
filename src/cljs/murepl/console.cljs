;; NOTE What's going on?
;; this is a straight up copy-pasta of himera's
;; repl.cljs. I tried including it modularly but it didn't work; we'll
;; be modifiying it anyway so this will have to work for now. I'll
;; include the license agreement as long as it makes sense to.

; Copyright (c) 2012, 2013 Fogus and Relevance Inc. All rights reserved.  The
; use and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns murepl.console
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.walk :as walk]
            [clojure.zip :as zip]))

(defn- map->js [m]
  (let [out (js-obj)]
    (doseq [[k v] m]
      (aset out (name k) v))
    out))

(defn go-eval [code]
  (let [data (atom nil)
        params (map->js {:url "/eval"
                         :data (str "{:expr " code "}")
                         :contentType "application/clojure; charset=utf-8"
                         :async false
                         :type "POST"
                         :dataType "text"
                         :success #(reset! data (reader/read-string %))})]
    (.ajax js/jQuery params)
    (.log js/console @data)
    @data))

(defn- on-validate [input]
  (not (empty? input)))

(defn- starts-with? [o s]
  (= (.slice (clojure.string/trim s)
             0
             (.-length o))
     o))

(def ^:private is-comment? #(starts-with? ";" %))

(defn- on-handle [line report]
  (if (is-comment? line)
    (build-msg "" "" "jquery-console-message-value")
    (let [input (.trim js/jQuery line)
          result (go-eval input)]
      (.log js/console result)
      (if-let [err (and result (:error result))]
        (build-msg "Compilation error: " err "jquery-console-message-error")
        (try
          (:msg result)
          (catch js/Error e
            (build-msg "Compilation error: " e "jquery-console-message-error")))))))

(defn ^:export go []
  (.ready (js/jQuery js/document)
          (fn []
            (set! js/controller
                    (.console (js/jQuery "#console") (map->js {:welcomeMessage "welcome to murepl."
                                        :promptLabel "> "
                                        :commandValidate on-validate
                                        :commandHandle on-handle
                                        :autofocus true
                                        :animateScroll true
                                        :promptHistory true}))))))
  
  
