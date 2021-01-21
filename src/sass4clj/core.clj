(ns sass4clj.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [sass4clj.util :as util]
    [sass4clj.webjars :as webjars]
    [cheshire.core :as json])
  (:import
    [java.net URI]
    [java.util Collection Collections]
    [io.bit3.jsass CompilationException Options Output OutputStyle Sass2ScssOptions]
    [io.bit3.jsass.importer Import Importer]))

(defn find-local-file [names current-dir]
  (some
    (fn [name]
      (let [f (io/file current-dir name)]
        (if (.exists f)
          [(.getPath f) f])))
    names))

(defn normalize-url
  "Simple URL normalization logic for import paths. Can normalize
  relative paths."
  [url-string]
  (loop [result nil
         parts (string/split url-string #"/")]
    (if (seq parts)
      (let [part (first parts)]
        (case part
          ;; Skip empty
          "" (recur result (rest parts))
          ;; Skip "."
          "." (recur result (rest parts))
          ;; Remove previous part, if there are previous non ".." parts
          ".." (if (and (seq result) (not= ".." (first result)))
                 (recur (rest result) (rest parts))
                 (recur (conj result part) (rest parts)))
          (recur (conj result part) (rest parts))))
      (string/join "/" (reverse result)))))

(defn join-url [& parts]
  (normalize-url (string/join "/" parts)))

(defn find-resource [names]
  (some (fn [name]
          (if-let [url (io/resource name)]
            (case (.getProtocol url)
              "file"
              [(.toString url) url]

              "jar"
              (let [jar-url (.openConnection url)
                    entry   (.getEntryName jar-url)]
                ; (util/dbug "Found %s from resources\n" url)
                [entry url]))))
        names))

(defn find-webjars [ctx names]
  (some (fn [name]
          (when-let [path (get (:asset-map ctx) name)]
            (util/dbug "found %s at webjars\n" path)
            (find-resource [path])))
        names))

(defn with-underscore [url]
  (let [parts (string/split url #"/")]
    (cond-> [url]
      (not (.startsWith (last parts) "_")) (conj (string/join "/" (conj (vec (butlast parts)) (str "_" (last parts))))))))

(defn remove-tilde-start [names]
  (mapcat (fn [name]
            (if (.startsWith name "~")
              [name (subs name 1)]
              [name]))
          names))

(defn possible-names [name]
  (let [scss? (.endsWith name ".scss")
        sass? (.endsWith name ".sass")
        css? (.endsWith name ".css")
        has-ext? (or scss? sass? css?)]
    (remove-tilde-start
      (cond-> []
        (or (not has-ext?) scss?) (into (with-underscore (if scss? name (str name ".scss"))))
        (or (not has-ext?) sass?) (into (with-underscore (if sass? name (str name ".sass"))))
        (or (not has-ext?) css?) (conj (if css? name (str name ".css")))))))

(defn sass2scss [source]
  (io.bit3.jsass.Compiler/sass2scss source (bit-xor Sass2ScssOptions/PRETTIFY2
                                                    Sass2ScssOptions/KEEP_COMMENT)))

(defn custom-sass-importer [ctx]
  (reify
    Importer
    (^Collection apply [this ^String import-url ^Import prev]
      ; (util/info "Import: %s\n" import-url)
      ; (util/info "Prev name: %s %s\n" (.getAbsoluteUri prev) (.getImportUri prev))
      (let [;; Generates different possibilies of names with _ and extensions added
            names (possible-names import-url)
            [_ parent] (re-find #"(.*)/([^/]*)$" (str (.getAbsoluteUri prev)))]
        ; (util/info "Parent: %s\n" parent)
        ; (util/info "Names: %s\n" names)
        (when-let [[found-absolute-uri uri]
                   (or (find-local-file names parent)
                       (some (fn [source-path]
                               (find-local-file names source-path))
                             (:source-paths ctx))
                       (find-resource names)
                       (find-resource (map #(join-url parent %) names))
                       (find-webjars ctx names))]
          ; (util/info "Import: %s, %s, result: %s\n" import-url uri found-absolute-uri)
          ; jsass doesn't know how to read content from other than files?
          ;; FIXME: If extension is sass, should convert the content to scss
          (Collections/singletonList
            (Import. import-url
                     found-absolute-uri
                     (cond-> (slurp uri)
                       (.endsWith found-absolute-uri ".sass") (sass2scss)))))))))

(def ^:private output-styles
  {:nested OutputStyle/NESTED
   :compact OutputStyle/COMPACT
   :expanded OutputStyle/EXPANDED
   :compressed OutputStyle/COMPRESSED})

(defn- build-options
  [{:keys [source-paths output-style source-map precision set-indented-syntax-src]}]
  (let [opts (Options.)
        include-paths (.getIncludePaths opts)]
    ;; Hardcode to use Unix newlines, mostly because that's what the tests use
    (.setLinefeed opts "\n")
    (doseq [source-path source-paths]
      (.add include-paths (io/file source-path)))
    (when output-style
      (.setOutputStyle opts (get output-styles output-style)))
    (when source-map
      ;; we manually append source-map uri in sass-compile-to-file
      (.setOmitSourceMapUrl opts true)
      ;; would be hard to deal with adding all the source-files to output...
      ;; or at least harder than just one file.
      (.setSourceMapContents opts true)
      (.setSourceMapFile opts (URI. "placeholder.css.map")))
    (when precision
      (.setPrecision opts precision))
    (.setIsIndentedSyntaxSrc opts (true? set-indented-syntax-src))
    opts))

(defn sass-compile
  "Input can be:
   - String
   - File

   Options:
   - :source-map-path - Enables source-maps and uses this URL for
     sourceMappingURL. Relative to css file."
  [input {:keys [verbosity source-map source-paths]
          :or {verbosity 1}
          :as options}]
  (binding [util/*verbosity* verbosity]
    (try
      (let [ctx {:asset-map (webjars/asset-map)
                 :source-paths source-paths}
            compiler (io.bit3.jsass.Compiler.)
            opts (build-options options)
            _ (doto (.getImporters opts)
                (.add (custom-sass-importer ctx)))
            output (if (string? input)
                     (.compileString compiler input opts)
                     (.compileFile compiler (.toURI input) nil opts))]
        {:output (.getCss output)
         :source-map (if source-map (.getSourceMap output))})
      (catch CompilationException e
        (throw (ex-info (.getMessage e) (assoc (json/parse-string (.getErrorJson e) true)
                                               :type ::error)))))))

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
        {:keys [output source-map] :as result} (sass-compile input-file options)
        source-map-url (str "/*# sourceMappingURL=" (.getName source-map-output) " */") ]
    (when output
      (io/make-parents output-file)
      (spit output-file output)
      (when source-map
        (spit output-file source-map-url :append true)
        (spit source-map-output source-map)))
    (if source-map
      (assoc result :output (str output source-map-url))
      result)))
