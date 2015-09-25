(ns sass4clj.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [sass4clj.util :as util]
    [sass4clj.webjars :as webjars])
  (:import
    [java.io IOException File]
    [java.net JarURLConnection URL URI]
    [java.util Collection Collections]
    [io.bit3.jsass CompilationException Options Output OutputStyle]
    [io.bit3.jsass.importer Import Importer]))

(defn find-local-file [file current-dir]
  (let [f (io/file current-dir file)]
    (if (.exists f)
      [(.getName f) (.getParent f)])))

(defn- url-parent [url]
  (let [[_ base name] (re-find #"(.*)/([^/]*)$" url)]
    [base (str base "/" name) :file]))

(defn- join-url [& parts]
  (string/join "/" parts))

(defn find-resource [url]
  (if url
    (case (.getProtocol url)
      "file"
      (let [[base name] (url-parent (str url))]
        [base name :resource url])

      "jar"
      (let [jar-url (.openConnection url)
            [base name] (url-parent (.getEntryName jar-url))]
        (util/dbug "Found %s from resources\n" url)
        [base name :resource url]))))

(defn find-webjars [ctx file]
  (when-let [path (get (:asset-map ctx) file)]
    (util/dbug "found %s at webjars\n" path)
    (find-resource (io/resource path))))

(defn add-ext [name]
  (if (.endsWith name ".scss")
    name
    (str name ".scss")))

(defn add-underscore [url]
  (let [parts (string/split url #"/")]
    (string/join "/" (conj (vec (butlast parts)) (str "_" (last parts))))))

(defn custom-sass-importer [ctx]
  (reify
    Importer
    (^Collection apply [this ^String url ^Import prev]
      ; (println "import" url)
      ; (util/info "Import: %s\n" url)
      ; (util/info "Prev name: %s base: %s\n" (.getUri prev) (.getBase prev))
      (let [url (add-ext url)
            [_ parent] (re-find #"(.*)/([^/]*)$" (str (.getUri prev)))]
        ; (util/info "Parent: %s\n" parent)
        (when-let [[base name type uri]
                   (or (find-local-file (add-underscore url) parent)
                       (find-local-file url parent)
                       (find-resource (io/resource (add-underscore url)))
                       (find-resource (io/resource url))
                       (find-resource (io/resource (add-underscore (join-url parent url))))
                       (find-resource (io/resource (join-url parent url)))
                       (find-webjars ctx (add-underscore url))
                       (find-webjars ctx url))]
          ; (util/info "Found base: %s name: %s\n" base name)
          ; jsass doesn't know how to read content from other than files?
          (Collections/singletonList
            (if (= :resource type)
              (Import. name base (slurp uri))
              (Import. name base))))))))

(def ^:private output-styles
  {:nested OutputStyle/NESTED
   :compact OutputStyle/COMPACT
   :expanded OutputStyle/EXPANDED
   :compressed OutputStyle/COMPRESSED})

(defn- build-options
  [{:keys [source-paths output-style]}]
  (let [opts (Options.)
        include-paths (.getIncludePaths opts)]
    (doseq [source-path source-paths]
      (.add include-paths (io/file source-path)))
    (when output-style
      (.setOutputStyle opts (get output-styles output-style)))
    opts))

(defn sass-compile
  "Input can be:
   - String
   - File"
  [input {:keys [verbosity]
          :or {verbosity 1}
          :as options}]
  (binding [util/*verbosity* verbosity]
    (try
      (let [ctx {:asset-map (webjars/asset-map)}
            compiler (io.bit3.jsass.Compiler.)
            opts (build-options options)
            _ (doto (.getImporters opts)
                (.add (custom-sass-importer ctx)))
            output (if (string? input)
                     (.compileString compiler input opts)
                     (.compileFile compiler (.toURI (io/file input)) nil opts))]
        ; TODO: .getErrorJson could be useful
        (when-not (zero? (.getErrorStatus output))
          (util/fail (.getErrorMessage output)))
        {:output (.getCss output)
         :source-map (.getSourceMap output)})
      (catch CompilationException e
        (util/fail (.getMessage e))
        {:error e}))))

(defn sass-compile-to-file [path target-dir relative-path options]
  (let [input-file (io/file path)
        output-file (io/file target-dir (string/replace relative-path #"\.scss$" ".css"))
        source-map-output (io/file target-dir (string/replace relative-path #"\.scss$" ".main.css.map"))
        {:keys [output source-map]} (sass-compile input-file options)]
    (when output
      (io/make-parents output-file)
      (spit output-file output)
      (when source-map (spit source-map-output source-map)))))
