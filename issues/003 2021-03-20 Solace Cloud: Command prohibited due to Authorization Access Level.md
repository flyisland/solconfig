
# 003 2021-03-20 Solace Cloud: Command prohibited due to Authorization Access Level

```commandline
java -jar build/libs/sempcfg.jar -H https://mrez79yr8kd4z.messaging.solace.cloud:943 -u demovpn-admin -p ijul41751k43ml6nvpf1o1gp6o update examples/template/demo_vpn.json
POST /msgVpns/demovpn/clientProfiles
{
  "allowBridgeConnectionsEnabled": true,
  "allowGuaranteedEndpointCreateEnabled": true,
  "allowGuaranteedMsgReceiveEnabled": true,
  "allowGuaranteedMsgSendEnabled": true,
  "allowSharedSubscriptionsEnabled": true,
  "clientProfileName": "testClientProfile"
}
{
  "count" : 0,
  "error" : {
    "code" : 72,
    "description" : "Problem with POST: Command prohibited due to Authorization Access Level",
    "status" : "UNAUTHORIZED"
  },
  "paging" : null,
  "request" : {
    "method" : "POST",
    "uri" : "https://mrez79yr8kd4z.messaging.solace.cloud:943/SEMP/v2/config/msgVpns/demovpn/clientProfiles"
  },
  "responseCode" : 400
}
```

## Cause analysis

