(ns sass4clj.core-test
  (:require [clojure.test :refer :all]
            [sass4clj.core :refer :all])
  (:import [java.io File]))

(deftest import-werbjars
  (is (:output (sass-compile "@import \"bootstrap/scss/bootstrap.scss\";" {:verbosity 3}))))

(deftest resource-hierarchy
  (is (:output (sass-compile (clojure.java.io/resource "susy-test.scss") {:verbosity 3}))))
