#!/usr/bin/env bash
native-image --verbose --no-fallback --no-server \
  --report-unsupported-elements-at-runtime \
  --enable-http --enable-https \
  --allow-incomplete-classpath \
  -jar ./build/libs/sempcfg.jar
