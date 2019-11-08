(ns sass4clj.api-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]
            [sass4clj.api :as sass])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(defn- temp-dir
  [prefix]
  (.toString (Files/createTempDirectory prefix (into-array FileAttribute []))))

(def ^:private includer-scss
  "body {
  color: red;
}

@import 'includee';
")

(def ^:private includee-sass
  "@charset 'utf-8'

p
  color: blue
")

(deftest include-paths
  (let [input-dir (temp-dir "sass4clj-input")
        include-dir (temp-dir "sass4clj-include")
        output-dir (temp-dir "sass4clj-output")
        options {:source-paths [input-dir include-dir]
                 :target-path  output-dir}]
    (spit (io/file input-dir "includer.scss") includer-scss)
    (spit (io/file include-dir "includee.sass") includee-sass)
    (sass/build options)
    (is (= "body {\n  color: red; }\n\np {\n  color: blue; }\n"
           (slurp (io/file output-dir "includer.css"))))))
