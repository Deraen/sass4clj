(defproject deraen/less4clj "0.1.1-SNAPSHOT"
  :description "Wrapper for Less4j"
  :url "https://github.com/deraen/less4clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0" :scope "provided"]
                 [org.webjars/webjars-locator "0.19"]
                 [org.slf4j/slf4j-nop "1.7.7"]
                 [com.github.sommeri/less4j "1.8.5"]
                 ; FIXME:
                 [boot/pod "2.0.0-rc8"]])
