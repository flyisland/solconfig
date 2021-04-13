# 005 #bug should valid the resource type

## Steps to reproduce this issue

```text
java -jar build/libs/solconfig.jar backup update /tmp/dempvpn.json
GET /null//tmp/dempvpn.json
{
  "count" : 0,
  "error" : {
    "code" : 535,
    "description" : "No paths found for /null//tmp/dempvpn.json",
    "status" : "INVALID_PATH"
  },
  "paging" : null,
  "request" : {
    "method" : "GET",
    "uri" : "https://mrez79yr8kd4z.messaging.solace.cloud:943/SEMP/v2/config/null//tmp/dempvpn.json"
  },
  "responseCode" : 400
}
```
