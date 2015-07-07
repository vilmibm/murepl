(ns murepl.comms
  (:require [clojure.tools.logging :as log]
            [puppetlabs.trapperkeeper.services :refer [defservice service-context]]
            [murepl.user :as u]
            [murepl.storage :as st]))

(defprotocol CommService
  (register!
    [this user channel]
    "Associates a user with a given channel.")

  (unregister!
    [this user]
    "Dissassociates a user from any associated channel.")

  (channel->user
    [this channel]
    "Given a channel, return nil or the user associated with this channel.")

  (send-msg!
    [this user]
    "Looks up the channel associated with a user and sends a message on it."))

(defservice comm-service
  CommService
  []
  (init [this ctx]
        (merge ctx {:users->channels (ref {})}))

  (register! [this user channel]
             (let [{:keys [users->channels]} (service-context this)]
               (if (contains? @users->channels (:name user))
                 (unregister! this user)
                 (dosync
                  (alter users->channels assoc (:name user) channel)
                  (log/infof "registered channel for %s" (:name user))))))

  (unregister! [this user]
               (let [{:keys [users->channels]} (service-context this)]
                 (dosync
                  (alter users->channels dissoc (:name user))
                  (log/infof "unregistered channel for %s" (:name user)))))

  (channel->user [this channel]
                 (let [{:keys [users->channels]} (service-context this)
                       if= (fn [x] (fn [[k v]] (if (= v x) k)))]
                   (if-let [username (some (if= channel) @users->channels)]
                     (u/lookup {:name username} st/db))))

  (send-msg! [this user] nil))

