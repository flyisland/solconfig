# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- add more output of `spec` subcommand

### Fixed
- issue 005: should valid the resource type of `backup` and `delete` subcommand
- exit the program while `delete` command with returned code other than `NOT_ALLOWED` or `CONFIGDB_OBJECT_DEPENDENCY`
- use java.net.URI instead of URLEncoder.encode, since the latter will translate space into "+" instead of "%20"

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
