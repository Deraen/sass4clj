(ns sass4clj.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [sass4clj.core :refer :all])
  (:import [java.io File]))

(deftest normalize-url-test
  (is (= "foo/bar" (normalize-url "foo/./bar")))
  (is (= "foo/bar" (normalize-url "foo//bar")))
  (is (= "bar" (normalize-url "foo/../bar")))
  (is (= "../foo" (normalize-url "../foo")))
  (is (= "../../foo" (normalize-url "../../foo")))
  (is (= "../../../foo" (normalize-url "../../../foo")))
  (is (= "../foo" (normalize-url "a/../../foo"))))

(deftest join-url-test
  (is (= "foo/bar" (join-url "foo" "bar")))
  (is (= "foo/bar" (join-url "foo" "bar")))
  (is (= "foo/bar" (join-url "foo/" "bar")))
  (is (= "foo/xxx" (join-url "foo/bar" "../xxx")))
  (is (= "foo bar/xxx" (join-url "foo bar" "xxx")))
  (is (= "foo%20bar/xxx" (join-url "foo%20bar" "xxx")))
  (is (= "a/d.less" (join-url "a/b/c" "../../d.less"))))

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
  (is (= {:output css :source-map nil}
         (sass-compile test-file {})))

  (is (= {:output css :source-map nil}
         (sass-compile sass {})))

  (is (= {:output css :source-map nil}
         (sass-compile local-test-file {}))))

(deftest sass-compile-source-map-test
  (let [out-file (File/createTempFile "sass4clj" "main.css")
        {:keys [output source-map]} (sass-compile-to-file local-test-file out-file {:source-map true})]
    (is (= (str "/*# sourceMappingURL=" (.getName out-file) ".map */")
           (last (string/split output #"\n"))))

    (is (= (str "/*# sourceMappingURL=" (.getName out-file) ".map */")
           (last (string/split (slurp out-file) #"\n"))))

    (is (string? source-map))))

(deftest import-werbjars
  (is (:output (sass-compile "@import \"bootstrap/scss/bootstrap.scss\";" {:verbosity 3}))))

(deftest compile-material-design-lite
  (is (:output (sass-compile "@import \"material-design-lite/src/material-design-lite\";" {}))))

(deftest sass-compile-error
  (is (thrown? clojure.lang.ExceptionInfo (sass-compile "foosdfsdf%;" {})))

  (try
    (sass-compile "foosdfsdf%;" {})
    (catch Exception e
      (let [error (ex-data e)]
        (is (= {:status 1
                :file "stdin"
                :line 1
                :column 1
                :message "Invalid CSS after \"f\": expected 1 selector or at-rule, was \"foosdfsdf%;\""
                :formatted "Error: Invalid CSS after \"f\": expected 1 selector or at-rule, was \"foosdfsdf%;\"\n        on line 1 of stdin\n>> foosdfsdf%;\n   ^\n"
                :type :sass4clj.core/error} error))))))

(def warning-file (doto (File/createTempFile "sass4clj" "warning-test.scss")
                    (spit "@warn \"test\";")))

(deftest sass-compile-warning
  (is (= nil
         (first (:warnings (sass-compile warning-file {}))))))
