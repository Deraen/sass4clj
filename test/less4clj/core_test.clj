(ns less4clj.core-test
  (:require [clojure.test :refer :all]
            [less4clj.core :refer :all])
  (:import [java.io File]))

(def less
"@test: #fff;
 @import \"foo.less\";
 a { color: @test;}")

(def css
"h1 {
  font-size: 12px;
}
a {
  color: #fff;
}
")

(def test-file (File/createTempFile "less4clj" "test.less"))
(spit test-file less)

(deftest less-compile-test
  (is (= {:output css :source-map nil} (less-compile test-file {})))
  (is (= {:output css :source-map nil} (less-compile less {}))))

(deftest import-werbjars
  (is (less-compile "@import \"bootstrap/less/bootstrap.less\";" {})))
