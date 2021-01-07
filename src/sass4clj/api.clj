(ns sass4clj.api
  (:require [sass4clj.watcher :as watcher]
            [sass4clj.core :as core]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(defn main-file? [file]
  (and (or (.endsWith (.getName file) ".scss")
           (.endsWith (.getName file) ".sass") )
       (not (.startsWith (.getName file) "_"))))

(defn find-main-files [source-paths {:keys [inputs]}]
  (mapcat (fn [source-path]
            (let [file (io/file source-path)]
              (->> (if inputs
                     (->> inputs
                          (map #(io/file file %))
                          (filter #(.exists %)))
                     (->> (file-seq file)
                          (filter main-file?)))
                   (map (fn [x] [(.getPath x) (.toString (.relativize (.toURI file) (.toURI x)))])))))
          source-paths))

(defn print-warning [warning]
  (println (format "WARN: %s %s\n" (:message warning)
                   (str (if (:uri (:source warning))
                          (str "on file "
                               (:uri (:source warning))
                               (if (:line warning)
                                 (str " at line " (:line warning) " character " (:char warning)))))))))

(defn compile-sass [main-files {:keys [auto target-path] :as options}]
  (doseq [[path relative-path] main-files]
    (println (format "Compiling {sass}... %s" relative-path))
    (let [result
          (try
            (core/sass-compile-to-file
              path
              (.getPath (io/file target-path (string/replace relative-path #"\.(scss|sass)$" ".css")))
              (dissoc options :target-path))
            (catch Exception e
              (if auto
                (println (.getMessage e))
                (throw e))))]
      (doseq [warning (:warnings result)]
        (print-warning warning)))))

(s/def ::source-paths (s/coll-of string? :into vec))
(s/def ::inputs (s/coll-of string? :into vec))
(s/def ::auto boolean?)
(s/def ::help boolean?)
(s/def ::target-path string?)
(s/def ::source-map boolean?)
(s/def ::verbosity #{1 2})
(s/def ::output-style #{:nested :compact :expanded :compressed})
(s/def ::options (s/keys :req-un [::source-paths ::target-path]
                         :opt-un [::inputs ::auto ::help ::source-map ::verbosity ::output-style]))

(defn build [{:keys [source-paths auto] :as options}]
  (when-not (s/valid? ::options options)
    (s/explain-out (s/explain-data ::options options)))
  (if auto
    (watcher/start source-paths (fn [& _]
                                  (let [main-files (find-main-files source-paths options)]
                                    (compile-sass main-files options))))
    (let [main-files (find-main-files source-paths options)]
      (compile-sass main-files options))))

(defn start [options]
  (build (assoc options :auto true)))

(defn stop [this]
  (if this
    (watcher/stop this)))
