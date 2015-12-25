(ns deraen.boot-sass
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [clojure.string  :as string]))

(def ^:private deps
  '[[deraen/sass4clj "0.1.1"]])

(defn by-pre
  [exts files & [negate?]]
  ((core/file-filter #(fn [f] (.startsWith (.getName f) %))) exts files negate?))

(defn- find-mainfiles [fs]
  (by-pre ["_"]
          (->> fs
               core/input-files
               (core/by-ext [".scss" ".sass"]))
          true))

(core/deftask sass
  "Compile Sass code.

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
            (pod/with-call-in @p
              (sass4clj.core/sass-compile-to-file
                ~input-path
                ~output-path
                {:verbosity ~(deref util/*verbosity*)
                 :output-style ~output-style})))))
        (-> fileset
            (core/add-resource output-dir)
            core/commit!))))
