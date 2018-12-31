(ns sass4clj.watcher
  (:require [hawk.core :as hawk]))

(defn start [source-paths f]
  (f)
  (hawk/watch!
    [{:paths source-paths
      :filter (fn [_ {:keys [file]}]
                (and (not (.startsWith (.getName file) "."))
                     (or (.endsWith (.getName file) ".scss")
                         (.endsWith (.getName file) ".sass")
                         (.endsWith (.getName file) ".css"))))
      :handler f}]))

(defn stop [watcher]
  (hawk/stop! watcher))
