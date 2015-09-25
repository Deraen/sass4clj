(defproject deraen/sass4clj "0.1.0-SNAPSHOT"
  :description "Wrapper for sass-java"
  :url "https://github.com/deraen/sass4clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0" :scope "provided"]
                 [com.cathive.sass/sass-java "3.2.5.0"]

                 ; For testing the webjars asset locator implementation
                 [org.webjars.bower/bootstrap "4.0.0-alpha" :exclusions [org.webjars.bower/jquery] :scope "test"]
                 [org.webjars/webjars-locator "0.19" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.7" :scope "test"]]
  :profiles {:dev {:dependencies []
                   :resource-paths ["test-resources"]}})
