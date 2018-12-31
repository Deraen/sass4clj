(ns sass4clj.integrant
  (:require [sass4clj.api :as api]
            [integrant.core :as ig]))

(defmethod ig/init-key :sass4clj [_ options]
  (api/start options))

(defmethod ig/halt-key! :sass4clj [this options]
  (api/stop this))

(defmethod ig/suspend-key! :sass4clj [this options]
  nil)

(defmethod ig/resume-key :sass4clj [key opts old-opts old-impl]
  (if (= opts old-opts)
    old-impl
    (do
      (ig/halt-key! key old-opts)
      (ig/init-key key opts))))
