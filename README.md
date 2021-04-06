# Backing Up and Restoring Solace PubSub+ Broker Configuration with SEMPv2 protocol

## Usage

Use the "backup" command to export the configuration of objects on a PS+ Broker into a single JSON, then use the "create" or "update" command to restore the configuration.

For example, you could run `./solconfig vpn Demo` to show the whole configuration on the console, or run `./solconfig vpn Demo > Demo.json` to save the configuration into a json file, use opaque password like `./solconfig -O 12345678 vpn Demo` if you want to back up the sensitive information like "password".

Then run `./solconfig -H http://another-broker:8080 create Demo.json` to create the same Message VPN on another broker.

## Installation

### Build

Make sure you have [JDK 11](https://adoptopenjdk.net/) or above installed.

```shell
git clone https://github.com/flyisland/solconfig
cd solconfig
./gradlew build
java -jar build/libs/solconfig.jar help
```
### Native Image

You could build `solconfig` as native image by following below steps:

1. Install [Graalvm](https://www.graalvm.org/docs/getting-started/#install-graalvm) 11 first.
```text
java -version
openjdk version "11.0.10" 2021-01-19
OpenJDK Runtime Environment GraalVM CE 21.0.0.2 (build 11.0.10+8-jvmci-21.0-b06)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.0.2 (build 11.0.10+8-jvmci-21.0-b06, mixed mode, sharing)
```
2. Then install the [Native Image](https://www.graalvm.org/reference-manual/native-image/), make sure you followed the [Prerequisites](https://www.graalvm.org/reference-manual/native-image/#prerequisites).

3. Run `./native-image.sh`, it will create a native image `./solconfig` which could be executed independently without JDK installed.

### [Download](https://github.com/flyisland/solconfig/releases)

## Help

Run `solconfig help` and `solconfig help sub-command` to check the help message carefully before you use it.

```text
$ java -jar solconfig.jar help
Usage: solconfig [-hkV] [--curl-only] [--cacert=<cacert>] [-H=<adminHost>]
               [-p=<adminPwd>] [-u=<adminUser>] [COMMAND]
Backing Up and Restoring Solace PubSub+ Broker Configuration with SEMPv2
protocol. Use the 'backup' command to export the configuration of objects on a
PS+  Broker into a single JSON, then use the 'create' or 'update' command to
restore the configuration.
      --cacert=<cacert>    CA certificate file to verify peer against when
                             using SSL
      --curl-only          Print curl commands only, no effect on 'backup'
                             command
  -h, --help               Show this help message and exit.
  -H, --host=<adminHost>   URL to access the management endpoint of the broker
                             Default: http://localhost:8080
  -k, --insecure           Allow insecure server connections when using SSL
  -p, --admin-password=<adminPwd>
                           The password of the management user
                             Default: admin
  -u, --admin-user=<adminUser>
                           The username of the management user
                             Default: admin
  -V, --version            Print version information and exit.
Commands:
  backup  Export the whole configuration of objects into a single JSON
  delete  Delete the specified objects
  create  Create objects from the configuration file
  update  Update the existing objects to make them the same as the
            configuration file

          Be careful, it will DELETE existing objects like Queues or Client
            Usernames, etc if they are absent in the configuration file.

          This "update" command is a good complement to "create" command,
            especially for the "default" VPN or the VPN of the Solace Cloud
            Service instance, since you can only update them.

  spec    Print the analyzed SEMPv2 specification
  help    Displays help information about the specified command
```

### backup

```text
$ java -jar solconfig.jar help backup
Usage: solconfig backup [-O=<opaquePassword>] <resourceType> <objectNames>...
Export the whole configuration of objects into a single JSON
      <resourceType>     Type of the exported object [cluster, vpn]
      <objectNames>...   One or more object names, , "*" means all
  -O, ---opaque-password=<opaquePassword>
                         The opaquePassword for receiving and updating opaque
                           properties like the password of Client Usernames
```

### delete

```text
$ java -jar solconfig.jar help delete
Usage: solconfig delete <resourceType> <objectName>
Delete the specified objects
      <resourceType>   Type of the object to delete [cluster, vpn]
      <objectName>     Object name to remove
```

### update

```text
$ java -jar solconfig.jar help update
Usage: solconfig update <confPath>
Update the existing objects to make them the same as the configuration file

Be careful, it will DELETE existing objects like Queues or Client Usernames,
etc if they are absent in the configuration file.

This "update" command is a good complement to "create" command, especially for
the "default" VPN or the VPN of the Solace Cloud Service instance, since you
can only update them.

      <confPath>   Configuration file
```

## Opaque Password

Before version 9.6.x (sempVersion 2.17), there is no way to get the value of "write-only" attributes like the password of Client Usernames, so that the backup output is not 100 percent as same as the configuration on the PS+ broker. Means you need to set those "write-only" values in the json file in clear text before you restore the configuration, or update them with the Solace PubSub+ Manager after you restore the configuration.

Since version 9.6.x (sempVersion 2.17), with a password is provided in the opaquePassword query parameter, attributes with the opaque property (like the password of Client Usernames) are retrieved in a GET in opaque form, encrypted with this password.

The backup output is now 100 percent as same as the configuration on the PS+ broker, and the same opaquePassword is used to restore the configuration. So you could restore them with the same opaquePassword.

The opaquePassword is only supported over HTTPS, and must be between 8 and 128 characters inclusive!

## Template support

Please check the [example](examples/template) for more details.