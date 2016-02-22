(ns deraen.boot-sass
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
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
               (core/by-ext [".main.scss" ".main.sass"]))
          true))

(core/deftask sass
  "SASS CSS compiler.

  For each `.main.sass` or `.main.scss` file in source-paths creates equivalent `.css` file.
  For example to create file `{target-path}/public/css/style.css` your sass
  code should be at path `{source-path}/public/css/style.main.scss`.

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
                        output-rel-path (string/replace (core/tmp-path f) #"\.main\.(scss|sass)$" ".css")
                        output-path (.getPath (io/file output-dir output-rel-path))]]
            (pod/with-call-in @p
              (sass4clj.core/sass-compile-to-file
                ~input-path
                ~output-path
                {:verbosity ~(deref util/*verbosity*)
                 :output-style ~output-style})))))
        (-> fileset
            (core/add-resource output-dir)
            core/commit!))))
