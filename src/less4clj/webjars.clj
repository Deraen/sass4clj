(ns less4clj.webjars
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.io IOException File]
           [java.lang ClassLoader]
           [java.util ServiceLoader]
           [java.util.jar JarFile JarEntry JarInputStream]))

(def WEBJARS_PATH_PREFIX "META-INF/resources/webjars")

(defn list-assets [classloader]
  (->> (.getResources classloader WEBJARS_PATH_PREFIX)
       enumeration-seq
       (reduce
         (fn [assets url]
           (concat
             assets
             (cond
               (= "jar" (.getProtocol url))
               (let [[_ jar] (re-find #"^file:(.*\.jar)\!/.*$" (.getPath url))]
                 (->> (enumeration-seq (.entries (JarFile. (io/file jar))))
                      (remove #(.isDirectory %))
                      (map #(.getName %))
                      (filter #(.startsWith % WEBJARS_PATH_PREFIX))))
               :else (throw (Exception. (str "less4clj.webjars doesn't know how to handle \"" (.getProtocol url) "\" urls"))))))
         [])
       set))

(def ^:private webjars-pattern
  #"META-INF/resources/webjars/([^/]+)/([^/]+)/(.*)")

(defn- asset-path [resource]
  (let [[_ name version path] (re-matches webjars-pattern resource)]
    (str name File/separator path)))

(defn asset-map []
  (->> (list-assets (.getContextClassLoader (Thread/currentThread)))
       (map (juxt asset-path identity))
       (into {})))

(comment
  (time (asset-map)))
