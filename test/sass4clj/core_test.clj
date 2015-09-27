(ns sass4clj.core-test
  (:require [clojure.test :refer :all]
            [sass4clj.core :refer :all])
  (:import [java.io File]))

(def sass
"$test: #fff;
@import \"foo.scss\";
a { color: $test;}")

(def css
"h1 {
  font-size: 12px; }

a {
  color: #fff; }
")

(def test-file (File/createTempFile "sass4clj" "test.scss"))
(spit test-file sass)

(def local-test-file (File/createTempFile "sass4clj" "local.scss"))
(spit local-test-file (str "@import \"" (.getName test-file) "\";"))

(deftest sass-compile-test
  (is (= {:output css :source-map nil} (sass-compile test-file {})))
  (is (= {:output css :source-map nil} (sass-compile sass {})))
  (is (= {:output css :source-map nil} (sass-compile local-test-file {}))))

(deftest import-werbjars
  (is (sass-compile "@import \"bootstrap/scss/bootstrap.scss\";" {:verbosity 3})))
