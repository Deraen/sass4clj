(ns sass4clj.webjars-test
  (:require [clojure.test :refer :all]
            [sass4clj.webjars :as webjars])
  (:import [org.webjars WebJarAssetLocator]))

(deftest list-assets-test
  (is (= (.listAssets (WebJarAssetLocator.) "")
         (webjars/list-assets (ClassLoader/getSystemClassLoader)))))
