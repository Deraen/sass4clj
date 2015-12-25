# boot-sass
[![Clojars Project](http://clojars.org/deraen/boot-sass/latest-version.svg)](http://clojars.org/deraen/boot-sass)

[Boot](https://github.com/boot-clj/boot) task to compile sass.

* Provides the `sass` task
* For each `.scss` or `.sass` file not starting with `_` in fileset creates equivalent `.css` file.
* Uses [jsass](https://github.com/bit3/jsass) through [sass4clj](https://github.com/Deraen/sass4clj) wrapper
    * Jsass requires Java 1.8.
* For parallel leiningen plugin check [lein-sass4clj](https://github.com/Deraen/lein-sass4clj)

## Usage

To create css file `public/css/main.css` have the scss file on path `public/css/main.scss` or use sift task to move the css file:
`(comp (sass) (sift :move {#"main.css" "public/css/main.css"}))`

## Features

- Load imports from classpath
  - Loading order. `@import "{name}";` at `{path}`.
    1. check if file `{path}/{name}.scss` exists
    2. try `(io/resource "{name}.scss")`
    3. try `(io/resource "{path}/{name}.scss")`
    4. check if webjars asset map contains `{name}`
      - Resource `META-INF/resources/webjars/{package}/{version}/{path}` can be referred using `{package}/{path}`
      - E.g. `bootstrap/scss/bootstrap.scss` => `META-INF/resources/webjars/bootstrap/4.0.0-alpha/scss/bootstrap.scss`
  - You should be able to depend on `[org.webjars.bower/bootstrap "4.0.0-alpha"]`
    and use `@import "bootstrap/scss/bootstrap";`
  - Use boot debug to find what is being loaded:
    `boot -vvv scss`

## License

Copyright © 2014-2015 Juho Teperi

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
