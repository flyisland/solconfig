#!/usr/bin/env bash
./gradlew build
native-image --verbose --no-fallback --no-server \
  --report-unsupported-elements-at-runtime \
  --enable-http --enable-https \
  --allow-incomplete-classpath \
  -jar ./build/libs/solconfig.jar
