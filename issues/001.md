# bug: Unable to delete aclProfiles and clientProfiles if the 'default' client-username configured against it in not shutdown state.

## Steps to reproduce

Create 'clientProfiles/testProfile' and 'aclProfiles/testACL' objects then configure the 'default' client-usernames to against them.

Then we'll get below error while trying to delete those objects:

```json
{
    "meta":{
        "error":{
            "code":490,
            "description":"Problem with DELETE: Can't delete client profile because there are client-usernames configured against it in not shutdown state.",
            "status":"CONFIGDB_OBJECT_DEPENDENCY"
        },
        "request":{
            "method":"DELETE",
            "uri":"http://localhost:8080/SEMP/v2/config/msgVpns/Demo/clientProfiles/testProfile"
        },
        "responseCode":400
    }
}
```
```json
{
    "meta":{
        "error":{
            "code":89,
            "description":"Problem with DELETE: Cannot delete the ACL profile because there are client-usernames configured against it in not shutdown state.",
            "status":"NOT_ALLOWED"
        },
        "request":{
            "method":"DELETE",
            "uri":"http://localhost:8080/SEMP/v2/config/msgVpns/Demo/aclProfiles/testACL"
        },
        "responseCode":400
    }
}
```

## Solution

Shutdown all the **default** objects while executing the `delete` command.