(ns less4clj.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [less4clj.util :as util]
    [less4clj.webjars :as webjars])
  (:import
    [java.io IOException File]
    [java.net JarURLConnection URL URI]
    [com.github.sommeri.less4j
     LessCompiler LessCompiler$Configuration Less4jException
     LessSource LessSource$FileNotFound LessSource$CannotReadFile LessSource$StringSourceException]
    [com.github.sommeri.less4j.core DefaultLessCompiler]))

(defn find-local-file [file current-dir]
  (let [f (io/file current-dir file)]
    (if (.exists f)
      [(.toURI f) (.getParent f) :file])))

(defn find-resource [file current-dir]
  (when-let [url (or (io/resource file) (io/resource (str current-dir "/" file)))]
    (if (= (.getProtocol url) "jar")
      (let [jar-url (.openConnection url)
            ; FIXME: regexing url
            [_ parent _] (re-find #"(.*)/([^/]*)$" (.getEntryName jar-url))]
        (util/dbug "Found %s from resources\n" url)
        [(.toURI url) parent :resource]))))

(defn find-webjars [ctx file]
  (if-let [path (get (:asset-map ctx) file)]
    (do
      (util/dbug "found %s at webjars\n" path)
      (find-resource path nil))))

(defn- not-found! []
  (throw (LessSource$FileNotFound.)))

(defn- cant-read! []
  (throw (LessSource$CannotReadFile.)))

(defn- slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (io/copy (io/input-stream x) out)
    (.toByteArray out)))

(defn custom-less-source
  [ctx type uri parent]
  (proxy [LessSource] []
    (relativeSource ^LessSource [^String import-filename]
      (util/dbug "importing %s at %s\n" import-filename parent)
      (if-let [[uri parent type]
               (or (find-local-file import-filename parent)
                   ; Don't search from other source-paths if looking for import from resource
                   (and (= type :file) (some #(find-local-file import-filename %) (:source-paths ctx)))
                   (find-resource import-filename parent)
                   (find-webjars ctx import-filename))]
        (custom-less-source ctx type uri parent)
        (not-found!)))
    (getContent ^String []
      (try
        (slurp uri)
        (catch Exception _
          (cant-read!))))
    (getBytes ^bytes []
      (try
        (slurp-bytes uri)
        (catch IOException e
          (cant-read!))))
    (getURI ^URI []
      uri)
    (getName ^String []
      (let [[_ name] (re-find #"([^/]*)$" (.toString uri))]
        name))))

(defn- build-configuration ^LessCompiler$Configuration
  [{:keys [source-map compression]}]
  (let [config (LessCompiler$Configuration.)
        source-map-config (.getSourceMapConfiguration config)]
    (doto config
      (.setCompressing (boolean compression)))
    (doto source-map-config
      (.setLinkSourceMap (boolean source-map))
      (.setIncludeSourcesContent true))
    config))

(defmulti ->less-source (fn [_ x] (class x)))

(defmethod ->less-source File [ctx file]
  (custom-less-source ctx :file (.toURI file) (.getParent file)))

(defn less-compile
  "Input can be:
   - File"
  [input {:keys [source-map source-paths] :as options}]
  (try
    (let [ctx {:source-paths source-paths
               :asset-map (webjars/asset-map)}
          result (-> (DefaultLessCompiler.)
                     (.compile
                       (->less-source ctx input)
                       (build-configuration options)))]
      (doseq [warn (.getWarnings result)]
        (util/warn "WARNING: %s\n" (.getMessage warn)))
      {:output (.getCss result)
       :source-map (if source-map (.getSourceMap result))})
    (catch Less4jException e
      (util/fail (.getMessage e))
      nil)))

(defn less-compile-to-file [path target-dir relative-path options]
  (let [input-file (io/file path)
        output-file (io/file target-dir (string/replace relative-path #"\.main\.less$" ".css"))
        source-map-output (io/file target-dir (string/replace relative-path #"\.main\.less$" ".main.css.map"))
        {:keys [output source-map] :as result} (less-compile input-file options)]
    (when result
      (io/make-parents output-file)
      (spit output-file output)
      (when source-map (spit source-map-output source-map)))))
