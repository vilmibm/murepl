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

(def ^:private error-class "jquery-console-message-error")
(def ^:private success-class "jquery-console-success")
(def ^:private blank-class "jquery-console-message-value")

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
    @data))

(defn- on-validate [input] (not (empty? input)))

(defn- starts-with? [o s]
  (= (.slice (clojure.string/trim s)
             0
             (.-length o))
     o))

(def ^:private is-comment? #(starts-with? ";" %))

(defn- build-msg
  [klass msg]
  (array (map->js {:msg msg :className klass})))

(def ^:private build-error (partial build-msg error-class))
(def ^:private build-success (partial build-msg success-class))
(def ^:private build-blank (partial build-msg blank-class ""))


(declare socket)

(defn- set-player-data [data]
  (.log js/console data)
  (.setItem (.-sessionStorage js/window) "player" data)
  (if (not (nil? socket))
      (.send socket (.-uuid (.parse js/JSON data)))))

(defn- get-player-data []
  (.getItem (.-sessionStorage js/window) "player"))

(defn- on-handle [line _]
  (if (is-comment? line)
    (build-blank)
    (let [input (.trim js/jQuery line)
          result (go-eval input)]
      (if-let [error-msg (:error result)]
        (build-error error-msg)
        (do
          (if-let [player-data (:player result)] (set-player-data player-data))
          (build-success (:msg result)))))))

(defn ^:export go []
  (.ready (js/jQuery js/document)
          (fn []
            (.ajaxPrefilter js/jQuery (fn [options _ _]
                                        (if-let [data (get-player-data)]
                                          (set! (.-headers options) (js-obj "player" data)))))
            (set! js/controller
                    (.console (js/jQuery "#console") 
                              (map->js {:welcomeMessage "welcome to murepl."
                                        :promptLabel "> "
                                        :commandValidate on-validate
                                        :commandHandle on-handle
                                        :autofocus true
                                        :animateScroll true
                                        :promptHistory true})))
            (def socket (js/WebSocket. "ws://localhost:8888/socket"))
            (set! (.-onmessage socket) (fn [data]
                                         (let [msg (.-data data)]
                                           (.log js/console "Got ws msg:" data)
                                           (.msg js/controller msg)))))))
