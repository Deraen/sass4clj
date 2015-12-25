(ns sass4clj.webjars-test
  (:require [clojure.test :refer :all]
            [sass4clj.webjars :as webjars]))

(deftest asset-map-test
  (is (contains? (webjars/asset-map) "bootstrap/less/bootstrap.less")))
