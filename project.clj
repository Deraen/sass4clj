(defproject deraen/less4clj "0.3.2"
  :description "Wrapper for Less4j"
  :url "https://github.com/deraen/less4clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0" :scope "provided"]
                 [com.github.sommeri/less4j "1.14.0"]

                 ; For testing the webjars asset locator implementation
                 [org.webjars/bootstrap "3.3.2" :scope "test"]
                 [org.webjars/webjars-locator "0.19" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.7" :scope "test"]]
  :profiles {:dev {:dependencies []
                   :resource-paths ["test-resources"]}})
