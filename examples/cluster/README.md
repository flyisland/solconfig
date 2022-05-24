# Create a DMR Cluster

## Topology

We'll create a DMR Cluster consist of two brokers, the `Local` and the `Remote`, and there is a internal link from the `Local` to the `Remote`.

    +-------+          +--------+
    | Local +--------->+ Remote |
    +-------+          +--------+

## Variables - vars.ftl

First, update the file `vars.ftl` according to your setup.

    <#assign dmrClusterName="cluster-test" >
    <#assign authenticationBasicPassword = "default" >
    <#assign localNodeName = "21bab8623480" >
    <#assign remoteNodeName = "sol01vm" >
    <#assign remoteAddress = "13.75.42.181" >

You could get the Node Name of the broker by running `show router-name` in CLI mode.

    29315f44a7eb> show router-name
    
    Router Name:          29315f44a7eb
    Mirroring Hostname:   Yes
    
    Deferred Router Name: 29315f44a7eb
    Mirroring Hostname:   Yes
    
    Unique Id:            07:32:b3:72:f2:b4:7a:62
    
    29315f44a7eb>
    
## How to Run It

    $solconfig -h localhost -u admin -p admin create ./cluster-local.json
    $solconfig -h remotehost -u admin -p admin create ./cluster-remote.json
