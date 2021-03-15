# Template Support

`sempcfg` employs the [FreeMarker template engine](https://freemarker.apache.org/) to provide template support for configuration file, so you could have more flexibility to define your PS+ broker.

The most used features are:

## [Include](https://freemarker.apache.org/docs/ref_directive_include.html)

The `include` directive lets you literally include different parts of configuration file together.  For example, you could have the queue's configuration in a separated file `queues.json`,  then include it into the message vpn configuration file `demo_vpn.json`.

## [Import](https://freemarker.apache.org/docs/ref_directive_include.html)

The `import` directive is used to define your variables together in a separated file. For example, you could have the number and name prefix of queue defined in the file `vars.ftl`, then import it in the `queues.json` file.

##[Apache FreeMarker Manual](https://freemarker.apache.org/docs/index.html)

Please check the official website for more details of FreeMarker.