### Changelog

All notable changes to this project will be documented in this file. Dates are displayed in UTC.

Generated by [`auto-changelog`](https://github.com/CookPete/auto-changelog).

#### [Unreleased](https://github.com/flyisland/solconfig/compare/v1.1.7...HEAD)

- fix: show each HTTP request/response while debug is enable [`20d298a`](https://github.com/flyisland/solconfig/commit/20d298a7bf17ae942c0f523a11c85b923d97e312)
- fix: add " --use-template" option to avoid the conflicts between the template and the Substitution Expressions [`87169ad`](https://github.com/flyisland/solconfig/commit/87169adf9af4039009cf2a899660972a23e15b81)
- fix: don't treat subscriptions start with "#" as reserved Object [`77aec0d`](https://github.com/flyisland/solconfig/commit/77aec0dea4396608eeaf89a884c803c0a36477d9)

#### [v1.1.7](https://github.com/flyisland/solconfig/compare/v1.1.6...v1.1.7)

> 3 November 2022

- fix, Targeted broker is on Solace Cloud, some objects will be ignored [`f0cffd2`](https://github.com/flyisland/solconfig/commit/f0cffd2638bcbaff04241190cc9f3d19c96f0e36)
- build: upgrade dependencies to the latest version [`b21c341`](https://github.com/flyisland/solconfig/commit/b21c3415fdd73067dca527f5938469df1cc8a912)
- release: v1.1.6 [`8400fe9`](https://github.com/flyisland/solconfig/commit/8400fe9ec58c412b7be298925bfb9d3678a7ee4d)

#### [v1.1.6](https://github.com/flyisland/solconfig/compare/1.1.5...v1.1.6)

> 28 March 2022

- feat: add checkIllegalAttributes() method [`b71798b`](https://github.com/flyisland/solconfig/commit/b71798b7d9f9b39ffe3dd185e1fa648fa48c4eda)
- feat: add "__skipThisObject" attribute to skip objects while performing `update` command [`6ed9b53`](https://github.com/flyisland/solconfig/commit/6ed9b533709827c3dcb2b57d6bec3763d105db7b)
- feat: add "--no-delete" option to command "update" [`9c35fe0`](https://github.com/flyisland/solconfig/commit/9c35fe0701de8a8b4d0bd37b31bb26a9b8f19e16)

#### [1.1.5](https://github.com/flyisland/solconfig/compare/v1.1.4...1.1.5)

> 7 February 2022

- refactor: use Optional to handle non existing IDENTIFYING attributes [`e0ebd80`](https://github.com/flyisland/solconfig/commit/e0ebd809c878916ded2c4c3b0fa79f7308bf0e13)
- fix: some objects like like "/msgVpns/bridges/remoteMsgVpns", have identifying attributes might not be required attributes [`52ed45e`](https://github.com/flyisland/solconfig/commit/52ed45e68b47aba70c4038567e754bda554af563)
- release: 1.1.5 [`9456a74`](https://github.com/flyisland/solconfig/commit/9456a7466c4e4485a8d7a78e72a14df08d163f2c)

#### [v1.1.4](https://github.com/flyisland/solconfig/compare/v1.1.3...v1.1.4)

> 18 September 2021

- fix #3, accept semp version like "2.11.00091010036" [`#3`](https://github.com/flyisland/solconfig/issues/3)
- open all ports [`fa8df0d`](https://github.com/flyisland/solconfig/commit/fa8df0df5bfcad970d7f2b4645766963219b5e90)
- add "--keep-default" option to `backup` command to indicate whether to Keep attributes with a default value. [`146f028`](https://github.com/flyisland/solconfig/commit/146f028fa7470df4a89a7bdd3356e08d9068f902)
- release 1.1.4 [`e045a49`](https://github.com/flyisland/solconfig/commit/e045a49a925b50f382aae87e1d93c8eccef01bea)
- hide subcommand `test` and `spec` since they are only useful for project developer [`ce1d681`](https://github.com/flyisland/solconfig/commit/ce1d681aa8d769925bd93d6392b899f1b42b3833)
- doc: update README.md on opaquePassword [`9b2b403`](https://github.com/flyisland/solconfig/commit/9b2b4036c6e2f5e4edfc15173d286b39bfe13c1e)
- sample: add mqtt ws port [`dae6ab1`](https://github.com/flyisland/solconfig/commit/dae6ab1eff6b4a46cc7fd7b8433b11c0bfc9e50f)
- fix, open web transport on 8008 [`8cded12`](https://github.com/flyisland/solconfig/commit/8cded1282a7980c948722c9566ef8636bdef4eb5)

#### [v1.1.3](https://github.com/flyisland/solconfig/compare/v1.1.2...v1.1.3)

> 28 June 2021

- close #1 [`#1`](https://github.com/flyisland/solconfig/issues/1)
- fix: create percentEncoding() method to encode identifying property. [`8328c40`](https://github.com/flyisland/solconfig/commit/8328c4014ad534f5b29771914898d97b03b7a4b1)
- fix: create percentEncoding() method to encode identifying property. [`ed25259`](https://github.com/flyisland/solconfig/commit/ed252598bbfd4c5d166e2ad6dba7f49a929f3be3)
- update README.md [`ba627dd`](https://github.com/flyisland/solconfig/commit/ba627dd4636d3698c334765d7f2dafe0e0cc1c98)

#### [v1.1.2](https://github.com/flyisland/solconfig/compare/v1.1.1...v1.1.2)

> 25 June 2021

- docs: update README.md [`896d292`](https://github.com/flyisland/solconfig/commit/896d2924bd1fddf64efd37d1457bd992a0281600)
- Use TreeMap to print spec in sorted order [`7ca49e0`](https://github.com/flyisland/solconfig/commit/7ca49e0bfd463082fe4dfe5360fc1ddd387b6dfa)
- issue: add 007 unable to update restTlsServerCertValidateNameEnabled attribute of VPN [`5841b74`](https://github.com/flyisland/solconfig/commit/5841b74d8482e194ea16ff06e474e3872dcae985)

#### [v1.1.1](https://github.com/flyisland/solconfig/compare/v1.1.0...v1.1.1)

> 6 April 2021

- fix: add generated graalvm configuration files into the git repo again [`c0a85ee`](https://github.com/flyisland/solconfig/commit/c0a85ee51aa9ab3089422bad09f86013756f2af9)
- fix: no need to put these graalvm config into the repo [`fe06c6b`](https://github.com/flyisland/solconfig/commit/fe06c6bb9d9a902b729401eb95e86334a42ba774)
- add test subcommand for integration test [`f10d280`](https://github.com/flyisland/solconfig/commit/f10d2803140414ed2e98fddd47993d514e2308d6)

#### [v1.1.0](https://github.com/flyisland/solconfig/compare/v1.0.3...v1.1.0)

> 1 April 2021

- rename package from 'sempcfg' to 'solconfig' [`906aaa5`](https://github.com/flyisland/solconfig/commit/906aaa57d9b85869282809ca4914bf47b8c7abe8)
- update docs: rename package from 'sempcfg' to 'solconfig' [`a58a222`](https://github.com/flyisland/solconfig/commit/a58a222845d73702f42c40f41058ec9e4f12642e)
- docs: add commend for method ConfigObject.checkAttributeCombinations(). [`da79cb4`](https://github.com/flyisland/solconfig/commit/da79cb43e873f7cf0a0f1dfec0d2e83e40dfa524)

#### [v1.0.3](https://github.com/flyisland/solconfig/compare/v1.0.2...v1.0.3)

> 29 March 2021

- add issue 004 [`0bd43af`](https://github.com/flyisland/solconfig/commit/0bd43af294d8d5feec312fb39564f0bd4aca9ca7)
- add method findAttributesCombinationsFromDescription() [`c7af5e3`](https://github.com/flyisland/solconfig/commit/c7af5e3360128ad975b916d2eee757dd38a9dd66)
- add method SempSpec.getRequiresAttributeWithDefaultValue() [`2bfa5f6`](https://github.com/flyisland/solconfig/commit/2bfa5f6ca80c24730dfcf9fd9888255f47f7c6c9)

#### [v1.0.2](https://github.com/flyisland/solconfig/compare/1.0.1...v1.0.2)

> 19 March 2021

- add Native Image Build Configuration files [`3b51fc4`](https://github.com/flyisland/solconfig/commit/3b51fc43265198cccdcf428220523c5110f88203)
- Add graalvm support of freemarker [`2259188`](https://github.com/flyisland/solconfig/commit/22591887393ecffecfbf654ff4ea6e23a60d9177)
- chore: add gradle wrapper [`7b64e88`](https://github.com/flyisland/solconfig/commit/7b64e8810aae44b412c8dccb5d5e19daf253d3f3)

#### [1.0.1](https://github.com/flyisland/solconfig/compare/1.0.0...1.0.1)

> 17 March 2021

- add: docker-compose.yml for create a Solace docker instance with tls enabled [`458b5c9`](https://github.com/flyisland/solconfig/commit/458b5c9a8f72a2b01f034ded36e1816ed64e7e34)
- release 1.0.1 [`0c4a5bb`](https://github.com/flyisland/solconfig/commit/0c4a5bb9f61fb150806553b9d01a6d0f2a661e72)
- feat: add `--insecure` and `--cacert [`ecc6c1b`](https://github.com/flyisland/solconfig/commit/ecc6c1bd955db2c3acbac84690cb9c422fbf5c52)

#### 1.0.0

> 16 March 2021

- Add JsonSpec class [`6a7b188`](https://github.com/flyisland/solconfig/commit/6a7b188bb0c7935ff73afd0e38704463be7e0f78)
- init commit [`294e061`](https://github.com/flyisland/solconfig/commit/294e0619c07bf4f0d129b3ea764736e1976b77b3)
- feat: update command [`f935333`](https://github.com/flyisland/solconfig/commit/f9353331a645cad6f52e121440c8b31fd115db4d)
