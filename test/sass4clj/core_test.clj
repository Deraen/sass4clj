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

(deftest with-underscore-test
  (is (= ["foo.scss" "_foo.scss"]
         (with-underscore "foo.scss")))
  (is (= ["_foo.scss"]
         (with-underscore "_foo.scss")))
  (is (= ["foo/bar.scss" "foo/_bar.scss"]
         (with-underscore "foo/bar.scss")))
  (is (= ["foo/_bar.scss"]
         (with-underscore "foo/_bar.scss"))))

(deftest possible-names-test
  (is (= ["foo.scss" "_foo.scss"]
         (possible-names "foo.scss")))
  (is (= ["foo/bar.scss" "foo/_bar.scss"]
         (possible-names "foo/bar.scss")))
  (is (= ["_foo.scss"]
         (possible-names "_foo.scss")))
  (is (= ["foo.sass" "_foo.sass"]
         (possible-names "foo.sass")))
  (is (= ["foo.css"]
         (possible-names "foo.css")))
  (is (= ["foo.scss" "_foo.scss" "foo.sass" "_foo.sass" "foo.css"]
         (possible-names "foo")))
  (is (= ["~bootstrap/foo/bar.scss" "bootstrap/foo/bar.scss"
          "~bootstrap/foo/_bar.scss" "bootstrap/foo/_bar.scss"
          "~bootstrap/foo/bar.sass" "bootstrap/foo/bar.sass"
          "~bootstrap/foo/_bar.sass" "bootstrap/foo/_bar.sass"
          "~bootstrap/foo/bar.css" "bootstrap/foo/bar.css"]
         (possible-names "~bootstrap/foo/bar"))) )

(def sass-file (File/createTempFile "sass4clj" "sass-file.sass"))
(def scss-file (File/createTempFile "sass4clj" "scss-file.scss"))

(def scss-code
"$test: #fff;

@import \"url.css\";
@import \"foo\";
@import \"xyz\";
@import \"bar\";

a { color: $test;}")

(def sass-code
"$test: red;

@import \"url.css\"
@import \"foo\"
@import \"xyz\"
@import \"bar\"

h1
  color: $test")

(def scss-css
"@import url(url.css);
.from-scss {
  font-size: 12px; }

.from-sass {
  color: salmon; }

.from-css {
  color: black; }

a {
  color: #fff; }
")

(def sass-css
"@import url(url.css);
.from-scss {
  font-size: 12px; }

.from-sass {
  color: salmon; }

.from-css {
  color: black; }

h1 {
  color: red; }
")

(spit scss-file scss-code)
(spit sass-file sass-code)

(def local-import-file (File/createTempFile "sass4clj" "local.scss"))
(spit local-import-file (str "@import \"" (.getName scss-file) "\";"))

(deftest sass-compile-test
  (is (= {:output scss-css :source-map nil}
         (sass-compile scss-file {})))

  (is (= {:output sass-css :source-map nil}
         (sass-compile sass-file {})))

  (is (= {:output scss-css :source-map nil}
         (sass-compile scss-code {})))

  ;; When compling string, syntax can't be detected from file name
  (is (= {:output sass-css :source-map nil}
         (sass-compile sass-code {:set-indented-syntax-src true})))

  (is (= {:output scss-css :source-map nil}
         (sass-compile local-import-file {}))))

(deftest sass-compile-source-map-test
  (let [out-file (File/createTempFile "sass4clj" "main.css")
        {:keys [output source-map]} (sass-compile-to-file local-import-file out-file {:source-map true})]
    (is (= (str "/*# sourceMappingURL=" (.getName out-file) ".map */")
           (last (string/split output #"\n"))))

    (is (= (str "/*# sourceMappingURL=" (.getName out-file) ".map */")
           (last (string/split (slurp out-file) #"\n"))))

    (is (string? source-map))))

(deftest import-werbjars
  (is (:output (sass-compile "@import \"bootstrap/scss/bootstrap.scss\";" {:verbosity 0})))

  (testing "webpack style import with ~, refering to Node package"
    (is (:output (sass-compile "@import \"~bootstrap/scss/bootstrap.scss\";" {:verbosity 0})))))

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
                :formatted "Error: Invalid CSS after \"f\": expected 1 selector or at-rule, was \"foosdfsdf%;\"\n        on line 1:1 of stdin\n>> foosdfsdf%;\n   ^\n"
                :type :sass4clj.core/error} error))))))

(def warning-file (doto (File/createTempFile "sass4clj" "warning-test.scss")
                    (spit "@warn \"test\";")))

(deftest sass-compile-warning
  (is (= nil
         (first (:warnings (sass-compile warning-file {}))))))
