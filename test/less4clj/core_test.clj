(ns less4clj.core-test
  (:require [clojure.test :refer :all]
            [less4clj.core :refer :all])
  (:import [java.io File]))

(def test-file (File/createTempFile "less4clj" "test.less"))
(spit test-file "@test: #fff;
                 a { color: @test;}")

(deftest less-compile-test
  (is (= "a {
  color: #fff;
}
"
         (:output (less-compile test-file {})))))
