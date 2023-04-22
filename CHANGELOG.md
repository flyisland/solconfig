# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
### Fixed
- fix: don't treat subscriptions start with "#" as reserved Object
- fix: retry the POST action if its dependencies are not found
- fix: add " --use-template" option to avoid the conflicts between the template and the Substitution Expressions 

## [1.1.7]
### Added
### Fixed
- fix, if Targeted broker is on Solace Cloud, some objects will be ignored. With Solace Cloud, objects Authentication, CA, and ClientProfiles should be managed by the Solace Cloud API
- fix, add "/msgVpns/restDeliveryPoints/restConsumers/oauthJwtClaims" to the SPEC_PATHS_OF_REQUIRES_DISABLE_CHILD list

## [1.1.6] - 2022-03-28
### Added
- add "__skipThisObject" attribute to skip objects while performing `update` command
- add "--no-delete" option to command "update"
### Fixed

## [1.1.5] - 2022-02-07
### Added
### Fixed
- [fix](https://github.com/flyisland/solconfig/commit/37129cf04f4b2fbccf54a8510736c2b5a5ae291a): Got NullPointerException while applying `create` or `backup` commands with only a file name with no folder prefix.
- fix: opposite action of "--keep-default" option of `backup` command
- fix: accept `adminHost` end with "/"
- feature: output debug information via environment variables `SOLCONFIG_LOGGING_LEVEL` set to "debug"
- fix: some objects like like "/msgVpns/bridges/remoteMsgVpns", have identifying attributes might not be required attributes

## [1.1.4] - 2021-09-18
### Added
- add "--keep-default" option to `backup` command to indicate whether to Keep attributes with a default value.
### Fixed
- fix: accept semp version like "2.11.00091010036", check https://github.com/flyisland/solconfig/issues/3

## [1.1.3] - 2021-06-28
### Fixed
- fix: create percentEncoding() method to encode identifying property.

## [1.1.2] - 2021-06-04
### Added
- add more output of `spec` subcommand

### Fixed
- issue 005: should valid the resource type of `backup` and `delete` subcommand
- exit the program while `delete` command with returned code other than `NOT_ALLOWED` or `CONFIGDB_OBJECT_DEPENDENCY`
- use java.net.URI instead of URLEncoder.encode, since the latter will translate space into "+" instead of "%20"
- disable the message-vpn before update attribute `restTlsServerCertValidateNameEnabled`
- OpaquePassword is only capable when the sempVersion of the config file is same as the broker's.

## [1.1.1] - 2021-04-06
### Added
- Native image support.

## [1.1.0] - 2021-04-01

Rename from 'sempcfg' to 'solconfig'.

## [1.0.3] - 2021-03-29
### Added
- Compare the Semp Version before apply the configuration
- Add 'spec' subcommand to show the analyzed SEMPv2 specification
  
### Fixed
- [issue 004](issues/x%20004%20%23bug,%20should%20make%20sure%20the%20%22Requires%22%20attributes%20are%20present.md)


## [1.0.2] - 2021-03-18
### Added
- Graalvm Native Image ready, check [README.md](README.md) to see how to build the native image.

## [1.0.1] - 2021-03-17
### Added
- support [Opaque Password](https://docs.solace.com/API-Developer-Online-Ref-Documentation/swagger-ui/config/index.html) to backup and restore passwords.
- "--insecure" and "--cacert" options to support PS+ broker with self-signed certification.

## [1.0.0] - 2021-03-16
### Added
- First release. Includes 4 sub-commands, `backup`, `delete`, `create` and `update`.
