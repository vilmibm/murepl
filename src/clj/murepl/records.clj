(ns murepl.records)

;; Things

(defrecord Player [id name desc])

(defrecord Item [id name desc])

(defrecord Room [id name desc exits])

;; Communication

(defrecord PlayerError [msg])

;; Actions

(defrecord MoveAction [player room])
