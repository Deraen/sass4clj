(ns leiningen.sass4clj
  (:require [leiningen.help]
            [leiningen.core.eval :as leval]
            [leiningen.core.project :as project]
            [leiningen.core.main :as main]
            [leiningen.help :as help]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.sass4clj.version :refer [+version+]]))

(defn main-file? [file]
  (and (or (.endsWith (.getName file) ".scss")
           (.endsWith (.getName file) ".sass") )
       (not (.startsWith (.getName file) "_"))))

(defn find-main-files [source-paths]
  (mapcat (fn [source-path]
            (let [file (io/file source-path)]
              (->> (file-seq file)
                   (filter main-file?)
                   (map (fn [x] [(.getPath x) (.toString (.relativize (.toURI file) (.toURI x)))])))))
          source-paths))

(def sass4j-profile {:dependencies [['deraen/sass4clj +version+]
                                    ['watchtower "0.1.1"]]})

; From lein-cljsbuild
(defn- eval-in-project [project form requires]
  (leval/eval-in-project
    project
    ; Without an explicit exit, the in-project subprocess seems to just hang for
    ; around 30 seconds before exiting.  I don't fully understand why...
    `(try
       (do
         ~form
         (System/exit 0))
       (catch Exception e#
         (do
           (if (= :sass4clj.core/error (:type (ex-data e#)))
             (println (.getMessage e#))
             (.printStackTrace e#))
           (System/exit 1))))
    requires))

(defn- run-compiler
  "Run the sasscss compiler."
  [project
   {:keys [source-paths target-path]
    :as options}
   watch?]
  (when-not target-path
    (main/abort "Lein-sass4clj requires :target-path option."))
  (let [project' (project/merge-profiles project [sass4j-profile])]
    (eval-in-project
      project'
      `(let [f# (fn compile-sass [& ~'_]
                  (doseq [[path# relative-path#] ~(vec (find-main-files source-paths))
                          :let [output-rel-path# (string/replace relative-path# #"\.(sass|scss)$" ".css")
                                output-path#     (.getPath (io/file ~target-path output-rel-path#))]]
                    (println (format "Compiling {sass}... %s" relative-path#))
                    (let [result#
                          (try
                            (sass4clj.core/sass-compile-to-file
                              path#
                              output-path#
                              ~(-> options
                                   (dissoc :target-path :source-paths)
                                   (update-in [:output-style] (fn [x] (if x (keyword x))))
                                   (update-in [:verbosity] (fn [x] (or x 1)))))
                            (catch Exception e#
                              (if ~watch?
                                (println (.getMessage e#))
                                (throw e#))))]
                      (doseq [message# (:warnings result#)]
                        (println (format "WARN: %s %s\n" message#))))))]
         (if ~watch?
           @(watchtower.core/watcher
             ~source-paths
             (watchtower.core/rate 100)
             (watchtower.core/file-filter watchtower.core/ignore-dotfiles)
             (watchtower.core/file-filter (watchtower.core/extensions :scss :sass))
             (watchtower.core/on-change f#))
           (f#)))
      '(require 'sass4clj.core 'watchtower.core))))

;; For docstrings

(defn- once
  "Compile sass files once."
  [project]
  nil)

(defn- auto
  "Compile sass files, then watch for changes and recompile until interrupted."
  [project]
  nil)

(defn sass4clj
  "SASS CSS compiler.

For each `.sass` or `.scss` file not starting with `_` in source-paths creates equivalent `.css` file.
For example to create file `{target-path}/public/css/style.css` your sass
code should be at path `{source-path}/public/css/style.scss`.

If you are seeing SLF4J warnings, check https://github.com/Deraen/sass4clj#log-configuration

Options should be provided using `:sass` key in project map.

Available options:
:target-path          The path where CSS files are written to.
:source-paths         Collection of paths where SASS files are read from.
:output-style         Possible types are :nested, :compact, :expanded and :compressed.
:verbosity            Set verbosity level, valid values are 1 and 2.
:source-map           Enable source-maps.

Other options are passed as is to sass4clj.

Command arguments:
Add `:debug` as subtask argument to enable debugging output."
  {:help-arglists '([once auto])
   :subtasks      [#'once #'auto]}
  ([project]
   (println (help/help-for "sass4clj"))
   (main/abort))
  ([project subtask & args]
   (let [args (set args)
         config (cond-> (:sass project)
                  (contains? args ":debug") (assoc :verbosity 2))]
     (case subtask
       "once" (run-compiler project config false)
       "auto" (run-compiler project config true)
       "help" (println (help/help-for "sass4clj"))
       (main/warn "Unknown task.")))))
