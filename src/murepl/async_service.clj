(ns murepl.async-service
  (:require [puppetlabs.trapperkeeper.services :refer [defservice service-context]]
            [murepl.async :as ma]))

(defprotocol AsyncService
  (user-join! [this user])
  (user-part! [this user]))

(defmacro with-channels [this channels-sym & body]
  `(let [~channels-sym (:channels (service-context ~this))]
     ~@body))

(defservice async-service
  AsyncService
  []
  (init [this ctx]
        (assoc ctx :channels (ma/init!)))

  (start [this ctx]
         (ma/go! (:channels ctx))
         ctx)

  (stop [this ctx]
        (ma/destroy! (:channels ctx))
        ctx)

  (user-join! [this user]
              (with-channels this chs
                (ma/user-join! chs user)))

  (user-part! [this user]
              (with-channels this chs
                (ma/user-part! chs user))))

