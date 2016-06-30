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
      [(-> f .getParentFile .toURI .toString) (.getName f) f])))

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
        [base name url])

      "jar"
      (let [jar-url (.openConnection url)
            [base name] (url-parent (.getEntryName jar-url))]
        (util/dbug "Found %s from resources\n" url)
        [base name url]))))

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
      ; (util/info "ImportUri: %s\n" (.getImportUri prev))
      ; (util/info "AbsoluteUri: %s\n" (.getAbsoluteUri prev))
      (let [url (add-ext url)
            parent (str (.getAbsoluteUri prev))]
        (when-let [[base name uri]
                   (or (find-local-file (add-underscore url) parent)
                       (find-local-file url parent)
                       (find-resource (io/resource (add-underscore (join-url parent url))))
                       (find-resource (io/resource (join-url parent url)))
                       (find-webjars ctx (add-underscore (join-url parent url)))
                       (find-webjars ctx (add-underscore url))
                       (find-webjars ctx url))]
          ; (util/info "Found base: %s name: %s\n" base name)
          ; jsass doesn't know how to read content from other than files?
          (Collections/singletonList
            (Import. name base (slurp uri))))))))

(def ^:private output-styles
  {:nested OutputStyle/NESTED
   :compact OutputStyle/COMPACT
   :expanded OutputStyle/EXPANDED
   :compressed OutputStyle/COMPRESSED})

(defn- build-options
  [{:keys [source-paths output-style source-map-path]}]
  (let [opts (Options.)
        include-paths (.getIncludePaths opts)]
    (doseq [source-path source-paths]
      (.add include-paths (io/file source-path)))
    (when output-style
      (.setOutputStyle opts (get output-styles output-style)))
    (when source-map-path
      (.setSourceMapRoot opts (URI. ""))
      (.setSourceMapFile opts (URI. source-map-path)))
    opts))

(defn sass-compile
  "Input can be:
   - String
   - File

   Options:
   - :source-map-path - Enables source-maps and uses this URL for
     sourceMappingURL. Relative to css file."
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
                     (.compileFile compiler (.toURI input) nil opts))]
        ; TODO: .getErrorJson could be useful
        {:output (.getCss output)
         :source-map (.getSourceMap output)})
      (catch CompilationException e
        (util/fail (.getErrorText e))
        ;{:error e}
        ))))

(defn sass-compile-to-file
  "Arguments:
   - input-path - Path to the input file
   - output-path - Path to the output file, possible to source map will be
     written to same path with `.map` appended
   - options

   Options:
   - :source-map - Enables source-maps and sets URI using output-path."
  [input-path output-path {:keys [source-map] :as options}]
  (let [input-file (io/file input-path)
        output-file (io/file output-path)
        source-map-name (if source-map (str output-path ".map"))
        source-map-output (io/file (str output-path ".map"))
        {:keys [output source-map]} (sass-compile input-file (assoc options :source-map-path source-map-name))]
    (when output
      (io/make-parents output-file)
      (spit output-file output)
      (when source-map (spit source-map-output source-map)))))
