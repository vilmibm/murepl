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
            [clojure.zip :as zip]
            [dommy.core :as d]
            [dommy.attrs :as da])
  (:use-macros [dommy.macros :only [sel1]]))
  

(def ^:private error-class "jquery-console-message-error")
(def ^:private success-class "jquery-console-success")
(def ^:private blank-class "jquery-console-message-value")

(defn- map->js [m]
  (let [out (js-obj)]
    (doseq [[k v] m]
      (aset out (name k) v))
    out))

(defn- map->str [m]
  (str "{"
       (str/join " " (for [[k v] m] (str k " \"" v "\"")))
       "}"))

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

(def ^:private player-data (atom nil))

(declare socket)

;; DEPRECATED
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
          (comment if-let [new-player-data (:player result)] 
            (if (nil? player-data) (set-player-data new-player-data)))
          (build-success (:msg result)))))))

(defn build-console [welcome-msg]
  (.console (js/jQuery "#console") 
            (map->js {:welcomeMessage welcome-msg
                      :promptLabel "> "
                      :commandValidate on-validate
                      :commandHandle on-handle
                      :autofocus false
                      :animateScroll true
                      :promptHistory true})))

(def welcome-msg
"Welcome to MUREPL!")

(defn toggle-auth []
  (js/alert "sup")
  (da/toggle! (sel1 "#auth-controls"))
  (da/toggle! (sel1 "#console")))

(defn- set-creds
  "Stores user credentials for request signing"
  [data]
  (reset! player-data data)
  (.send socket (:uuid @player-data)))

(defn auth-error [msg]
  (js/alert msg))

(defn handle-auth-error-response [error-resp]
  (let [clj (reader/read-string (.-responseText error-resp))]
    (auth-error (:error clj))))

(defn login-handler [e]
  (.preventDefault e)
  (let [data     (atom nil)
        name (.-value (sel1 "#login input[name=name]"))
        password (.-value (sel1 "#login input[name=password]"))
        params   (map->js {:url "/login"
                           :data (map->str {:name name :password password})
                           :contentType "application/clojure; charset=utf-8"
                           :async false
                           :type "POST"
                           :dataType "text"
                           :success #(do 
                                       (reset! data (reader/read-string %))
                                       (js/alert "HIIII")
                                       (set-creds {:name name :password password})
                                       (toggle-auth))
                           :error handle-auth-error-response})]
    (.ajax js/jQuery params)
    @data))

(defn create-handler [e]
  (.preventDefault e)
  (let [data (atom nil)
        name     (.-value (sel1 "#create input[name=\"name\"]"))
        password (.-value (sel1 "#create input[name=\"password\"]"))
        desc     (.-value (sel1 "#create input[name=\"desc\"]"))
        params   (map->js {:url "/create"
                           :data (map->str {:name name 
                                            :password password
                                            :desc desc})
                           :contentType "application/clojure; charset=utf-8"
                           :async false
                           :type "POST"
                           :dataType "text"
                           :success  #(do
                                       (reset! data (reader/read-string %))
                                       (set-creds {:name name :password password})
                                       (toggle-auth))
                           :error handle-auth-error-response})]
    (.log js/console (map->str {:name name 
                                :password password
                                :desc desc}))
    (.ajax js/jQuery params)
    @data))

(defn setup-listeners
  []
  (let [login  (sel1 "#login")
        create (sel1 "#create")]
    (d/listen! login  :submit login-handler)
    (d/listen! create :submit create-handler)))


(defn main []
  (.ajaxPrefilter js/jQuery (fn [options _ _]
                              (if-let [data player-data]
                                (set! (.-headers options) (js-obj "player" data)))))
  (def socket (js/WebSocket. (str/join "" ["ws://" (.-hostname (.-location js/window)) ":8889/socket"])))
  (setup-listeners)
  (set! js/controller (build-console welcome-msg))
  (set! (.-onmessage socket) (fn [data]
                               (let [msg (.-data data)]
                                 (.log js/console "Got ws msg:" data)
                                 (.msg js/controller msg)))))

(defn ^:export go []
  (.ready (js/jQuery js/document) main))
