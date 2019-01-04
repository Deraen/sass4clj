(ns sass4clj.watcher
  (:require [hawk.core :as hawk]))

;; From figwheel-main
;; https://github.com/bhauman/figwheel-main/blob/be9c7b87d115cc1db807e0de7d9a763519be3e2d/src/figwheel/main/watching.clj#L41
(defn throttle [millis f]
  (fn [{:keys [collector] :as ctx} e]
    (let [collector (or collector (atom {}))
          {:keys [collecting? events]} (deref collector)]
      (if collecting?
        (swap! collector update :events (fnil conj []) e)
        (do
          (swap! collector assoc :collecting? true)
          (future (Thread/sleep millis)
                  (let [events (volatile! nil)]
                    (swap! collector
                           #(-> %
                                (assoc :collecting? false)
                                (update :events (fn [evts] (vreset! events evts) nil))))
                    (f (cons e @events))))))
      (assoc ctx :collector collector))))

(defn start [source-paths f]
  (f)
  (hawk/watch!
    [{:paths source-paths
      :filter (fn [_ {:keys [file]}]
                (and (not (.startsWith (.getName file) "."))
                     (or (.endsWith (.getName file) ".scss")
                         (.endsWith (.getName file) ".sass")
                         (.endsWith (.getName file) ".css"))))
      :handler (throttle 50 f)}]))

(defn stop [watcher]
  (hawk/stop! watcher))
