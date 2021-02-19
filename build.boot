(def +version+ "0.5.5-SNAPSHOT")

(set-env!
  :resource-paths #{"src"}
  :source-paths #{"test" "test-resources"}
  :dependencies   '[[org.clojure/clojure "1.10.1" :scope "provided"]
                    [metosin/bat-test "0.4.3" :scope "test"]

                    [io.bit3/jsass "5.10.3"]
                    [cheshire "5.9.0"]

                    [org.webjars/webjars-locator "0.37"]
                    [hawk "0.2.11"]
                    [org.clojure/tools.cli "0.4.2"]

                    [com.stuartsierra/component "0.4.0" :scope "test"]
                    [suspendable "0.1.1" :scope "test"]
                    [integrant "0.7.0" :scope "test"]

                    ;; Webjars-locator uses logging
                    [org.slf4j/slf4j-nop "1.7.29" :scope "test"]

                    ;; For testing the webjars asset locator implementation
                    [org.webjars.bower/bootstrap "4.3.1" :scope "test"]
                    [org.webjars.bower/material-design-lite "1.3.0" :scope "test"]])

(require '[metosin.bat-test :refer [bat-test]])

(task-options!
  pom {:version     +version+
       :url         "https://github.com/deraen/sass4clj"
       :scm         {:url "https://github.com/deraen/sass4clj"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(defn with-files
  "Runs middleware with filtered fileset and merges the result back into complete fileset."
  [p middleware]
  (fn [next-handler]
    (fn [fileset]
      (let [merge-fileset-handler (fn [fileset']
                                    (next-handler (commit! (assoc fileset :tree (merge (:tree fileset) (:tree fileset'))))))
            handler (middleware merge-fileset-handler)
            fileset (assoc fileset :tree (reduce-kv
                                          (fn [tree path x]
                                            (if (p x)
                                              (assoc tree path x)
                                              tree))
                                          (empty (:tree fileset))
                                          (:tree fileset)))]
        (handler fileset)))))

(deftask write-version-file
  [n namespace NAMESPACE sym "Namespace"]
  (let [d (tmp-dir!)]
    (fn [next-handler]
      (fn [fileset]
        (let [f (clojure.java.io/file d (-> (name namespace)
                                            (clojure.string/replace #"\." "/")
                                            (clojure.string/replace #"-" "_")
                                            (str ".clj")))]
          (clojure.java.io/make-parents f)
          (spit f (format "(ns %s)\n\n(def +version+ \"%s\")" (name namespace) +version+)))
        (next-handler (-> fileset (add-resource d) commit!))))))

(deftask build []
  (comp
    (with-files
      (fn [x] (and (re-find #"sass4clj" (tmp-path x))
                   (not (re-find #"leiningen" (tmp-path x)))))
      (comp
        (pom
          :project 'deraen/sass4clj
          :description "Clojure wrapper for jsass")
        (jar)
        (install)))

    (with-files
      (fn [x] (re-find #"boot_sass" (tmp-path x)))
      (comp
        (pom
          :project 'deraen/boot-sass
          :description "Boot task to compile SASS"
          :dependencies [])
        (write-version-file :namespace 'deraen.boot-sass.version)
        (jar)
        (install)))

    (with-files
      (fn [x] (re-find #"leiningen" (tmp-path x)))
      (comp
        (pom
          :project 'deraen/lein-sass4clj
          :description "Leinigen task to compile SASS"
          :dependencies [])
        (write-version-file :namespace 'leiningen.sass4clj.version)
        (jar)
        (install)))))

(deftask dev []
  (comp
   (watch)
   (repl :server true)
   (build)
   (target)))

(deftask deploy []
  (comp
   (build)
   (push :repo "clojars" :gpg-sign (not (.endsWith +version+ "-SNAPSHOT")))))

(ns-unmap *ns* 'test)

(deftask test []
  (comp
    (write-version-file :namespace 'sass4clj.version)
    (bat-test :report 'eftest.report.pretty/report)))

(deftask autotest []
  (comp
    (watch)
    (test)))
