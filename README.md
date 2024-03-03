# This plugin is still work in progress!

# Jenkins: pipeline-multi-module-plugin
This plugin will provide a distinctive way to define & discover projects in the pipeline.
All projects found can than be activated or deactivated on individual conditions like: Is this module's version already published?, Is this module still a snapshot? or Are dependencies affected by CVEs?

## Integrations
Currently we have following integrations in mind ...

### Git
Allow the checking/creation of git tags to evaluate if a module has already be published.

(We usually do this in our repositories, since the official mirrors usually take multiple hours to update!)

### Maven
When using maven we en/disable modules as previously defined. So you don't have to!'

(This typically discovers wrong versions at compile time (or during unit tests) since an outdated version would get pulled from a mirror instead using the locally compiled up to date version!)

We currently investigate whether the maven integration should be reimplemented from scratch or contributed to [jenkinsci/pipeline-maven-plugin](https://github.com/jenkinsci/pipeline-maven-plugin) as an optional feature dependent on this plugin.

