## 0.3.0 (x.4.2015)

- *BREAKING CHANGE*: `less-compile` now returns a map with
  resulting css and source-map as strings
- `less-compile-to-file` creates output files

## 0.2.1 (21.3.2015)

- Fixed error logging for non boot use

## 0.2.0 (3.3.2015)

- Replaced WebjarsAsset locator with a simple clojure implementation
  - Might be missing some stuff but I have tested this to work with Boostrap
  - Doesn't require java logging lib
- Updated to less4j 1.9.0
