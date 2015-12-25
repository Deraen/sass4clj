#!/bin/bash

set -e

echo "1.7.0"
BOOT_CLOJURE_VERSION=1.7.0 boot run-tests

echo
echo "1.6.0"
BOOT_CLOJURE_VERSION=1.6.0 boot run-tests

echo
echo "1.8.0-RC4"
BOOT_CLOJURE_VERSION=1.8.0-RC4 boot run-tests
