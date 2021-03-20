# 002 2021-03-15 #bug Problem with POST: Cannot change connect-via unless link is disabled

## Steps to reproduce

This error happens while we try to create a `remoteAddresses` for a `link`, like below:

```
POST /dmrClusters OK
POST /dmrClusters/cluster-test/links OK
POST /dmrClusters/cluster-test/links/sol01vm/remoteAddresses
{
  "count" : 0,
  "error" : {
    "code" : 89,
    "description" : "Problem with POST: Cannot change connect-via unless link is disabled",
    "status" : "NOT_ALLOWED"
  },
  "paging" : null,
  "request" : {
    "method" : "POST",
    "uri" : "http://localhost:8080/SEMP/v2/config/dmrClusters/cluster-test/links/sol01vm/remoteAddresses"
  },
  "responseCode" : 400
}
```

## Cause analysis

It seems that why trying to create or delete some kind of objects, their parent object needs to be disabled status.

```
curl -X DELETE  -u $SEMP_ADMIN:$SEMP_PWD http://localhost:8080/SEMP/v2/config/dmrClusters/cluster-test/links/sol01vm/remoteAddresses/13.75.42.181
{
    "meta":{
        "error":{
            "code":89,
            "description":"Problem with DELETE: Cannot change connect-via unless link is disabled",
            "status":"NOT_ALLOWED"
        },
        "request":{
            "method":"DELETE",
            "uri":"http://localhost:8080/SEMP/v2/config/dmrClusters/cluster-test/links/sol01vm/remoteAddresses/13.75.42.181"
        },
        "responseCode":400
    }
}
```

## Solution

Disable the parent object before update those child objects, and then enable the parent back.