
## Steps to reproduce

Just update the VPN with attribute restTlsServerCertValidateNameEnabled set to true.

```text
{
  "count" : 0,
  "error" : {
    "code" : 89,
    "description" : "Problem with restTlsServerCertValidateNameEnabled: Not Allowed: Message-vpn is enabled with rest-delivery-point 'aws' enabled and rest-consumer 'aws REST Consumer' enabled that is using Remote SSL",
    "status" : "NOT_ALLOWED"
  },
  "paging" : null,
  "request" : {
    "method" : "PUT",
    "uri" : "http://localhost:8080/SEMP/v2/config/msgVpns/RDP"
  },
  "responseCode" : 400
}
```
## Cause analysis

Although attribute `restTlsServerCertValidateNameEnabled` is not a "Requires-Disable" attribute of object Message-vpn, but it indeed requires the Message-vpn (or rest-delivery-point or rest-consumer) to be disabled first to update it.

## Solution
