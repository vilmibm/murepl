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
(declare player-data)

(defn set-player-data [data]
  (.log js/console "SETTING DATA" data)
  (def player-data data)
  (if (not (nil? socket))
    (let [uuid (.-uuid (.parse js/JSON data))]
      (.log js/console "SENDING TO SOCKET")
      (.send socket uuid))))

(defn- on-handle [line _]
  (if (is-comment? line)
    (build-blank)
    (let [input (.trim js/jQuery line)
          result (go-eval input)]
      (.log js/console "RESULT" result)
      (.log js/console "PLAYERDATER" (:player result))
      (if-let [error-msg (:error result)]
        (build-error error-msg)
        (do
          (if-let [new-player-data (:player result)] 
            (if (nil? player-data) (set-player-data new-player-data)))
          (build-success (:msg result)))))))

(defn build-console [welcome-msg]
  (.console (js/jQuery "#console") 
            (map->js {:welcomeMessage welcome-msg
                      :promptLabel "> "
                      :commandValidate on-validate
                      :commandHandle on-handle
                      :autofocus true
                      :animateScroll true
                      :promptHistory true})))

(def welcome-msg
"Welcome to MUREPL!
If you already have a character, run (connect \"your name\" \"your password\").
If not, try (new-player \"your name\" \"a plaintext password\" \"a description of yourself\" )
")

(defn ^:export go []
  (.ready (js/jQuery js/document)
          (fn []
            (.ajaxPrefilter js/jQuery (fn [options _ _]
                                        (if-let [data player-data]
                                          (set! (.-headers options) (js-obj "player" data)))))
            (def socket (js/WebSocket. "ws://162.243.29.87:8888/socket"))
            (set! js/controller (build-console welcome-msg))
            (set! (.-onmessage socket) (fn [data]
                                         (let [msg (.-data data)]
                                           (.log js/console "Got ws msg:" data)
                                           (.msg js/controller msg)))))))
