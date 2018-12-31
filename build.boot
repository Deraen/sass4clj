(def +version+ "0.4.0-SNAPSHOT")

(set-env!
  :resource-paths #{"src"}
  :source-paths #{"test" "test-resources"}
  :dependencies   '[[org.clojure/clojure "1.9.0" :scope "provided"]
                    [metosin/bat-test "0.4.0" :scope "test"]

                    [io.bit3/jsass "5.5.2"]
                    [cheshire "5.7.1"]

                    [org.webjars/webjars-locator "0.32-1"]
                    [hawk "0.2.11"]
                    [org.clojure/tools.cli "0.4.1"]

                    [com.stuartsierra/component "0.3.2" :scope "test"]
                    [suspendable "0.1.1" :scope "test"]
                    [integrant "0.7.0" :scope "test"]

                    ;; Webjars-locator uses logging
                    [org.slf4j/slf4j-nop "1.7.25" :scope "test"]

                    ;; For testing the webjars asset locator implementation
                    [org.webjars.bower/bootstrap "4.0.0-alpha.6" :scope "test"]
                    [org.webjars.bower/material-design-lite "1.3.0" :scope "test"]])

(require '[metosin.bat-test :refer [bat-test]])

(task-options!
  pom {:version     +version+
       :url         "https://github.com/deraen/sass4clj"
       :scm         {:url "https://github.com/deraen/sass4clj"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

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
    (write-version-file :namespace 'sass4clj.version)
    (pom
      :project 'deraen/sass4clj
      :description "Clojure wrapper for jsass")
    (aot
      :namespace #{'sass4clj.main})
    (jar :main 'sass4clj.main)
    (install)))

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
