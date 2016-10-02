(ns deraen.boot-sass
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [boot.file       :as file]
   [clojure.string  :as string]
   [deraen.boot-sass.version :refer [+version+]]))

(def ^:private deps
  [['deraen/sass4clj +version+]])

(defn by-pre
  [exts files & [negate?]]
  ((core/file-filter #(fn [f] (.startsWith (.getName f) %))) exts files negate?))

(defn- find-mainfiles [fs]
  (by-pre ["_"]
          (->> fs
               core/input-files
               (core/by-ext [".scss" ".sass"]))
          true))

(defn- find-relative-path [dirs filepath]
  (if-let [file (io/file filepath)]
    (let [parent (->> dirs
                      (map io/file)
                      (some (fn [x] (if (file/parent? x file) x))))]
      (if parent (.getPath (file/relative-to parent file))))))

(defn- find-original-path [source-paths dirs filepath]
  (if-let [rel-path (find-relative-path dirs filepath)]
    (or (some (fn [source-path]
                (let [f (io/file source-path rel-path)]
                  (if (.exists f)
                    (.getPath f))))
              source-paths)
        rel-path)
    filepath))

(defn- fixed-message
  "Replaces the tmp-path in formatted error message using path in working dir."
  [{:keys [formatted file]}]
  (let [correct-path (find-original-path (concat (:source-paths pod/env) (:resource-paths pod/env))
                                         (:directories pod/env)
                                         file)]
    (string/replace formatted #"(on line \d* of )(.*)" (fn [[_ prefix wrong-path]]
                                                         ;; FIXME: search file using wrong-path
                                                         (str prefix correct-path)))))

(core/deftask sass
  "SASS CSS compiler.

  For each `.sass` or `.scss` file not starting with `_` in source-paths creates equivalent `.css` file.
  For example to create file `{target-path}/public/css/style.css` your sass
  code should be at path `{source-path}/public/css/style.scss`.

  If you are seeing SLF4J warnings, check https://github.com/Deraen/sass4clj#log-configuration

  Output-styles:
  - :nested
  - :compact
  - :expanded
  - :compressed"
  [o output-style STYLE kw "Set output-style"]
  (let [output-dir  (core/tmp-dir!)
        p           (-> (core/get-env)
                        (update-in [:dependencies] into deps)
                        pod/make-pod
                        future)
        prev        (atom nil)]
    (core/with-pre-wrap fileset
      (let [sources (->> fileset
                         (core/fileset-diff @prev)
                         core/input-files
                         (core/by-ext [".scss" ".sass"]))]
        (reset! prev fileset)
        (when (seq sources)
          (util/info "Compiling {sass}... %d changed files.\n" (count sources))
          (doseq [f (find-mainfiles fileset)
                  :let [input-path (.getPath (core/tmp-file f))
                        output-rel-path (string/replace (core/tmp-path f) #"\.(scss|sass)$" ".css")
                        output-path (.getPath (io/file output-dir output-rel-path))]]
            (let [{:keys [error]}
                  (pod/with-call-in @p
                    (sass4clj.core/sass-compile-to-file
                      ~input-path
                      ~output-path
                      {:verbosity ~(deref util/*verbosity*)
                       :output-style ~output-style}))]
              (when error
                (throw (Exception. (fixed-message error)))))))
        (-> fileset
            (core/add-resource output-dir)
            core/commit!)))))
