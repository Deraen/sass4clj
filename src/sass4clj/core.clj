(ns sass4clj.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [sass4clj.util :as util]
    [sass4clj.webjars :as webjars])
  (:import
    [java.io IOException File]
    [java.net JarURLConnection URL URI]
    [com.cathive.sass SassCompilationException SassContext SassFileContext
     SassOptions SassOutputStyle]
    [java.nio.file Path]))

(defn find-local-file [file current-dir]
  (let [f (io/file current-dir file)]
    (if (.exists f)
      [(.toURI f) (.getParent f) :file])))

(defn- url-parent [url]
  (let [[_ x] (re-find #"(.*)/([^/]*)$" url)]
    x))

(defn- join-url [& parts]
  (string/join "/" parts))

(defn find-resource [url]
  (if url
    (case (.getProtocol url)
      "file"
      [(.toURI url) (url-parent (str url)) :resource]

      "jar"
      (let [jar-url (.openConnection url)
            parent (url-parent (.getEntryName jar-url))]
        (util/dbug "Found %s from resources\n" url)
        [(.toURI url) parent :resource]))))

(defn find-webjars [ctx file]
  (if-let [path (get (:asset-map ctx) file)]
    (do
      (util/dbug "found %s at webjars\n" path)
      (find-resource (io/resource path)))))

; (defn- not-found! []
;   (throw (LessSource$FileNotFound.)))

; (defn- cant-read! []
;   (throw (LessSource$CannotReadFile.)))

(defn- slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream x) out)
    (.toByteArray out)))

; (defn custom-sass-source
;   [ctx type uri parent]
;   (proxy [LessSource] []
;     (relativeSource ^LessSource [^String import-filename]
;       (util/dbug "importing %s at %s\n" import-filename parent)
;       (if-let [[uri parent type]
;                (or (find-local-file import-filename parent)
;                    ; Don't search from other source-paths if looking for import from resource
;                    (and (= type :file) (some #(find-local-file import-filename %) (:source-paths ctx)))
;                    (find-resource (io/resource import-filename))
;                    (find-resource (io/resource (join-url parent import-filename)))
;                    (find-webjars ctx import-filename))]
;         (custom-sass-source ctx type uri parent)
;         (not-found!)))
;     (getContent ^String []
;       (try
;         (slurp uri)
;         (catch Exception _
;           (cant-read!))))
;     (getBytes ^bytes []
;       (try
;         (slurp-bytes uri)
;         (catch IOException e
;           (cant-read!))))
;     (getURI ^URI []
;       uri)
;     (getName ^String []
;       (let [[_ name] (re-find #"([^/]*)$" (.toString uri))]
;         name))))

; (defn inline-sass-source
;   [ctx source]
;   (proxy [LessSource] []
;     (relativeSource ^LessSource [^String import-filename]
;       (util/dbug "importing %s at inline sass\n")
;       (if-let [[uri parent type]
;                (or (some #(find-local-file import-filename %) (:source-paths ctx))
;                    (find-resource (io/resource import-filename))
;                    (find-webjars ctx import-filename))]
;         (custom-sass-source ctx type uri parent)
;         (not-found!)))
;     (getContent ^String []
;       source)
;     (getBytes ^bytes []
;       (.getBytes source))))

(defn- set-options
  [ctx {:keys [source-paths source-map compression]}]
  (doto (.getOptions ctx)
    (.setIncludePaths (make-array Path (map #(.toPath %) source-paths)))))

(defn sass-compile
  "Input can be:
   - File"
  [input {:keys [source-map source-paths] :as options}]
  (try
    (let [ctx (SassFileContext/create (.toPath input))
          _ (set-options ctx options)
          ; TODO: Use streams in API
          out (java.io.ByteArrayOutputStream.)
          _ (.compile out)
          result (str out)]
      {:output result
       :source-map nil})
    (catch SassCompilationException e
      (util/fail (.getMessage e))
      {:error e})))
