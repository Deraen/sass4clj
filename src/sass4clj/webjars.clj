(ns sass4clj.webjars
  (:import [java.io File]
           [org.webjars WebJarAssetLocator]))

(def ^:private webjars-pattern
  #"META-INF/resources/webjars/([^/]+)/([^/]+)/(.*)")

(defn- asset-path [resource]
  (let [[_ name version path] (re-matches webjars-pattern resource)]
    (str name File/separator path)))

(defn asset-map
  "Create map of asset path to classpath resource url. Asset path is
  the resource url without webjars part."
  []
  (->> (vals (.getFullPathIndex (WebJarAssetLocator.)))
       (map (juxt asset-path identity))
       (into {})))

(comment
  (time (asset-map)))
