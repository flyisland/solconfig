#!/usr/bin/env bash
mkdir -p ./src/main/resources/META-INF/native-image/solconfig
java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/solconfig \
  -jar build/libs/solconfig.jar $@