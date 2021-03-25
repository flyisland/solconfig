# 004 #bug, should make sure the "Requires" attributes are present.

## Steps to reproduce this issue

[attachments/vpn.json](attachments/vpn.json)

```commandline
java -jar build/libs/sempcfg.jar update ../rdp_demo/vpn.json
POST /msgVpns/RDP/restDeliveryPoints OK
POST /msgVpns/RDP/restDeliveryPoints/demo/queueBindings OK
POST /msgVpns/RDP/restDeliveryPoints/demo/restConsumers
{
  "enabled": true,
  "remoteHost": "host.docker.internal",
  "remotePort": 9999,
  "restConsumerName": "mock"
}
{
  "count" : 0,
  "error" : {
    "code" : 228,
    "description" : "Missing attribute \"tlsEnabled\", required with \"remotePort\".",
    "status" : "MISSING_PARAMETER"
  },
  "paging" : null,
  "request" : {
    "method" : "POST",
    "uri" : "http://localhost:8080/SEMP/v2/config/msgVpns/RDP/restDeliveryPoints/demo/restConsumers"
  },
  "responseCode" : 400
}
```

## Cause analysis

1. the update() method calls removeAttributesWithDefaultValue() method
1. the default value of "tlsEnabled" is false, and was removed

## Solution


