# Sass4clj
[![Clojars Project](http://clojars.org/deraen/sass4clj/latest-version.svg)](http://clojars.org/deraen/sass4clj)

Clojure wrapper for [jsass](https://github.com/bit3/jsass/) JNA wrapper for Libsass.

## Usage

Check [boot-sass](https://github.com/Deraen/boot-sass) and [lein-sass4clj](https://github.com/Deraen/lein-sass4clj).

## Features

- Load imports from classpath
  - Loading order. `@import "{name}";` at `{path}`.
    1. check if file `{path}/{name}.sass` exists
    2. try `(io/resource "{name}.sass")`
    3. try `(io/resource "{path}/{name}.sass")`
    4. check if webjars asset map contains `{name}`
      - Resource `META-INF/resources/webjars/{package}/{version}/{path}` can be referred using `{package}/{path}`
      - E.g. `bootstrap/sass/bootstrap.scss` => `META-INF/resources/webjars/bootstrap/4.0.0-alpha/scss/bootstrap.scss`
  - You should be able to depend on `[org.webjars/bootstrap "3.3.1"]`
    and use `@import "bootstrap/sass/bootstrap";`

## License

Copyright © 2014-2015 Juho Teperi

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
