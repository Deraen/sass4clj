(ns sass4clj.component
  (:require [sass4clj.api :as api]
            [com.stuartsierra.component :as component]
            [suspendable.core :as suspendable]))

(defrecord Sass4Clj [options]
  component/Lifecycle
  (start [this]
    (assoc this :watcher (api/start options)))
  (stop [this]
    (api/stop (:watcher this)))
  suspendable/Suspendable
  (suspend [this]
    this)
  (resume [this old-this]
    (if (= (:options this) (:options old-this))
      old-this
      (do
        (component/stop old-this)
        (component/start this)))))
