## 0.5.4 (2021-02-19)

**[compare](https://github.com/Deraen/sass4clj/compare/0.5.3...0.5.4)**

- Fix lein plugin when `:source-paths` is merged from multiple lein profiles

## 0.5.3 (2021-01-21)

**[compare](https://github.com/Deraen/sass4clj/compare/0.5.2...0.5.3)**

- Read imports from other source-paths before the classpath
- Print resolved imports on debug log

## 0.5.2 (2021-01-07)

**[compare](https://github.com/Deraen/sass4clj/compare/0.5.1...0.5.2)**

- Set exit code in `-main` and Lein plugin if errors occured

## 0.5.1 (2020-02-07)

**[compare](https://github.com/Deraen/sass4clj/compare/0.5.0...0.5.1)**

- Fix `:source-paths` not being passed to compile file, so files
from another source-paths folder couldn't be imported. ([#31](https://github.com/Deraen/sass4clj/pull/31/))

## 0.5.0 (2019-10-03)

**[compare](https://github.com/Deraen/sass4clj/compare/0.4.1...0.5.0)**

- Support mixed Scss & Sass projects (include `.sass` files from `.scss` files) ([#22](https://github.com/Deraen/sass4clj/issues/22))

## 0.4.1 (2019-08-28)

**[compare](https://github.com/Deraen/sass4clj/compare/0.4.0...0.4.1)**

- Fix the Integrant namespace ([#30](https://github.com/Deraen/sass4clj/pull/30))

## 0.4.0 (2019-08-20)

**[compare](https://github.com/Deraen/sass4clj/compare/0.3.1...0.4.0)**

- **Breaking**:
    - Requires Clojure 1.9 (for spec)
- Sass4clj now contains main namespace for `clj` use
- Sass4clj new has `sass4clj.api` namespace with easy to use `start` and `stop` functions
- Add [Integrant](https://github.com/weavejester/integrant) namespace `sass4clj.integrant` namespace
- Add [Component](https://github.com/stuartsierra/component) namespace `sass4clj.component` namespace
- Use [Hawk](https://github.com/wkf/hawk/) for watching for file changes, this should work better on OS X
- Support Webpack style import paths, which support starting the path with `~`
when referring to Node packages.
- Add `inputs` option which can be used to select main files

## 0.3.1 (8.3.2017)

**[compare](https://github.com/Deraen/sass4clj/compare/0.3.0...0.3.1)**

- Fixed a bad macro in Leiningen plugin which broke less4clj with Clojure 1.9-alpha14

## 0.3.0 (18.10.2016)

**[compare](https://github.com/Deraen/sass4clj/compare/0.2.1...0.3.0)**

- Run tests on Windows CI
- Update to latest jsass ([#6](https://github.com/Deraen/sass4clj/pull/6))
- Supports source maps ([#1](https://github.com/Deraen/sass4clj/pull/1))
- Support raw css imports ([#14](https://github.com/Deraen/sass4clj/pull/14))

## 0.2.1 (22.2.2016)

**[compare](https://github.com/Deraen/sass4clj/compare/0.2.0...0.2.1)**

- Fixed the documentation about main files
    - Main files don't need to end with `.main.ext`
    - Main files are the files not starting with `_`
- Fix URI construction on Windows ([#7](https://github.com/Deraen/sass4clj/pull/7))

## 0.2.0 (25.12.2015)

- Synchronized versions between all packages
- Boot and Lein packages are now maintained in sass4clj repository

## 0.1.1 (27.9.2015)

- Fixed local file imports

## 0.1.0 (26.9.2015)
