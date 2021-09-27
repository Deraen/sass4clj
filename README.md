# Sass4clj
[![Clojars Project](https://img.shields.io/clojars/v/deraen/sass4clj.svg)](https://clojars.org/deraen/sass4clj)
[![Build Status](https://travis-ci.org/Deraen/sass4clj.svg?branch=master)](https://travis-ci.org/Deraen/sass4clj)
[![AppVeyor](https://img.shields.io/appveyor/ci/deraen/sass4clj.svg?maxAge=2592000&label=windows)](https://ci.appveyor.com/project/Deraen/sass4clj)

Clojure wrapper for [jsass](https://github.com/bit3/jsass/) JNA wrapper for Libsass.
This repository also contains [Boot](http://boot-clj.com/) and [Leiningen](http://leiningen.org/) tasks.

For parallel Less library check [less4clj](https://github.com/Deraen/less4clj)

## ATTENTION: [libsass](https://sass-lang.com/blog/libsass-is-deprecated) (the C library) and the JNA wrapper library [jsass](https://github.com/bit3/jsass/) are deprecated. Consider using [Dart Sass](https://sass-lang.com/dart-sass) if you do not need to read SCSS files from the Java classpath.

Both sass4clj still works and will receive bug fixes, but difference
between libsass and dart-sass will continue growing.

Some ideas if you need to read read files from the classpath or jar files:

- It might be possible to extend dart-sass through [Importer API](https://pub.dev/documentation/sass/latest/sass/Importer-class.html)
- Just extract files from the jar files in a script before calling dart-sass compile (for example using `clj` and `unzip`)

## Features

- Jsass features
    - Requires Java 1.8
    - Linux & Windows builds are automatically tested
    - (Doesn't work on Alpine based Docker images)
- Load imports directly from Java classpath (e.g. Webjars)
    - Add dependency `[org.webjars.bower/bootstrap "4.0.0-alpha"]` to use [Bootstrap](http://getbootstrap.com/)
- Assumes that files starting with `_` are [partial files](http://sass-lang.com/guide) and should not be compiled into CSS files.

## Boot [![Clojars Project](https://img.shields.io/clojars/v/deraen/boot-sass.svg)](https://clojars.org/deraen/boot-sass)

* Provides the `sass` task (`deraen.boot-sass/sass`)
* Select main files using `inputs` option
* or, for each `.sass` or `.scss` file not starting with `_` in the fileset creates equivalent `.css` file.
* Check `boot sass --help` for task options.

## Leiningen [![Clojars Project](https://img.shields.io/clojars/v/deraen/lein-sass4clj.svg)](https://clojars.org/deraen/lein-sass4clj)

* Provides the `sass4clj` task
* Select main files using `inputs` option
* or, for each `.sass` or `.scss` file not starting with `_` in source-dirs creates equivalent `.css` file.
* Check `lein help sass4clj` for options.

## Clj

Test in the repository:

`clj -m sass4clj.main --source-paths test-resources`

Check `clj -m sass4clj.main --help` for options.

## Import load order

Loading order for `@import "{name}";` on file at `{path}`

1. Local file at `{path}/{name}.sass` or `{path}/{name}.scss`
2. Local files on other source-paths, `{source-path}/{name}.ext`
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

Copyright © 2014-2017 Juho Teperi

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
