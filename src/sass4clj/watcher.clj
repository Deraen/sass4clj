(ns sass4clj.watcher
  (:require [nextjournal.beholder :as beholder]))

(defn start [source-paths f]
  (f)
  (let [watcher (apply beholder/watch
                       (fn [{:keys [_type path]}]
                         (let [fname (.toString (.getFileName path))]
                           (when (and (not (.startsWith fname "."))
                                      (or (.endsWith fname ".scss")
                                          (.endsWith fname ".sass")
                                          (.endsWith fname ".css")))
                             (f))))
                       source-paths)
        wait (promise)]
    ;; Keep JVM running while watcher is active
    (doto (Thread. (fn [] @wait)) .start)
    {:watcher watcher
     :wait wait}))

(defn stop [{:keys [watcher wait]}]
  (beholder/stop watcher)
  (deliver wait nil))

(comment
  (stop (start "src" (fn [_]))))
