# Sass4clj [![Clojars Project](https://img.shields.io/clojars/v/deraen/sass4clj.svg)](https://clojars.org/deraen/sass4clj) [![Build Status](https://travis-ci.org/Deraen/sass4clj.svg?branch=master)](https://travis-ci.org/Deraen/sass4clj)

Clojure wrapper for [jsass](https://github.com/bit3/jsass/) JNA wrapper for Libsass.
This repository also contains [Boot](http://boot-clj.com/) and [Leiningen](http://leiningen.org/) tasks.

For parallel Less library check [less4clj](https://github.com/Deraen/less4clj)

## Features

- Jsass requires Java 1.8.
- Load imports directly from Java classpath (e.g. Webjars)
    - Add dependency `[org.webjars.bower/bootstrap "4.0.0-alpha"]` to use [Bootstrap](http://getbootstrap.com/)

## Boot [![Clojars Project](https://img.shields.io/clojars/v/deraen/boot-sass.svg)](https://clojars.org/deraen/boot-sass)

* Provides the `sass` task (`deraen.boot-sass/sass`)
* For each `.main.sass` or `.main.scss` file in the fileset creates equivalent `.css` file.
* Check `boot sass --help` for task options.

## Leiningen [![Clojars Project](https://img.shields.io/clojars/v/deraen/lein-sass4clj.svg)](https://clojars.org/deraen/lein-sass4clj)

* Provides the `sass4clj` task
* For each `.main.sass` or `.main.scss` file in source-dirs creates equivalent `.css` file.
* Check `lein help sass4clj` for options.

## Import load order

Loading order for `@import "{name}";` on file at `{path}`

1. Local file at `{path}/{name}.sass` or `{path}/{name}.scss`
2. Classpath resource `(io/resource "{name}.ext")`
3. Classpath resource `(io/resource "{path}/{name}.ext")`
4. Webjar asset
    - Resource `META-INF/resources/webjars/{package}/{version}/{path}` can be referred using `{package}/{path}`
    - For example `@import "bootstrap/scss/bootstrap.scss";` will import  `META-INF/resources/webjars/bootstrap/4.0.0-alpha/scss/bootstrap.scss`

## FAQ

### Log configuration

If you don't have any slf4j implementations you will see a warning:

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

To disable this add a no operation logger to your project. As this is only required
on build phase, you can use `:scope "test"` so that the dependency is not
transitive and is not included in uberjar. Alternatively you can add this
dependency to your Leiningen dev profile.

```
[org.slf4j/slf4j-nop "1.7.13" :scope "test"]
```

## License

Copyright © 2014-2016 Juho Teperi

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
