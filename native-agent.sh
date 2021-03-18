#!/usr/bin/env bash
mkdir -p ./src/main/resources/META-INF/native-image/sempcfg
java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/sempcfg \
  -jar build/libs/sempcfg.jar create examples/template/demo_vpn.json