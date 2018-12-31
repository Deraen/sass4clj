(ns sass4clj.main
  (:require [sass4clj.api :as api]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(def cli-opts
  [["-h" "--help"]
   ["-a" "--auto" "Enable file watcher"]
   ["-t" "--target-path TARGET" "The path where CSS files are written to"
    :default "target"]
   ["-s" "--source-map" "Enable source-maps for compiled CSS"]
   ["-v" "--verbosity LEVEL" "Set verbosity level, valid values are 1 and 2."
    :parse-fn (fn [s]
                (Integer/parseInt s))
    :validate [#{1 2} "Must be 1 or 2"]]
   [nil "--source-paths PATHS" "List of LESS source paths, comma separated"
    :default ["src"]
    :parse-fn (fn [x]
                (str/split x #","))]])

(defn help-text [options-summary]
(str "{less} CSS compiler.

Usage: program-name [options]

For each `.main.less` file in source-paths creates equivalent `.css` file.
For example to create file `{target-path}/public/css/style.css` your less
code should be at path `{source-path}/public/css/style.main.less`.

If you are seeing SLF4J warnings, check https://github.com/Deraen/sass4clj#log-configuration

Options:
" options-summary))

(defn -main [& args]
  (let [{:keys [options summary errors]} (cli/parse-opts args cli-opts)
        {:keys [help]} options]
    (cond
      errors (println (str/join "\n" errors))
      help (println (help-text summary))
      :else (api/build options))))
