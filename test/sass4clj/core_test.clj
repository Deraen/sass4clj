(ns sass4clj.core-test
  (:require [clojure.test :refer :all]
            [sass4clj.core :refer :all])
  (:import [java.io File]))

(def sass
"@test: #fff;
 @import \"foo.sass\";
 a { color: @test;}")

(def css
"h1 {
  font-size: 12px;
}
a {
  color: #fff;
}
")

(def test-file (File/createTempFile "sass4clj" "test.scss"))
(spit test-file sass)

(deftest sass-compile-test
  (is (= {:output css :source-map nil} (sass-compile test-file {})))
  (is (= {:output css :source-map nil} (sass-compile sass {}))))

(deftest import-werbjars
  (is (sass-compile "@import \"bootstrap/sass/bootstrap.scss\";" {})))
