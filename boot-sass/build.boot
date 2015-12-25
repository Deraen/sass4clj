(set-env!
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.6.0"       :scope "provided"]
                    [boot/core           "2.2.0"       :scope "provided"]
                    [adzerk/bootlaces    "0.1.11"      :scope "test"]
                    [deraen/sass4clj     "0.1.1"       :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.1")

(bootlaces! +version+)

(task-options!
  pom {:project     'deraen/boot-sass
       :version     +version+
       :description "Boot task to compile sass."
       :url         "https://github.com/deraen/boot-sass"
       :scm         {:url "https://github.com/deraen/boot-sass"}
       :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (pom)
    (jar)
    (install)))
