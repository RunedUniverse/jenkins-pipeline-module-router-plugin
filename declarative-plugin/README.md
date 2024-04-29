# Declarative Plugin

**State: SUSPENDED**

**Reason: Upstream Issues**

**Details:**

Since the plugin tries to add declarative options, when-conditions and a custom stages this plugin has to extend the [pipeline-model-definition-plugin](https://github.com/jenkinsci/pipeline-model-definition-plugin) (current version: `2.2198.v41dd8ef6dd56`).

They seem to try to provide support for extensions but they, by the looks of it, never tried it and don't have a tutorial or example showing how to.

Some of the underlying issues include:
+ Instead of the Groovy Source code the Compiled Intermediary Java Code is bundled!
+ Several variables that a child-class might need to access are marked as (implicit) `private` instead of `protected`.
+ Required constructors are marked as (implicit) `private` instead of `protected`.
+ Generally lots of methods have to be overridden and copied over due to narrow minded implementations, see `org.jenkinsci.plugins.pipeline.modulerouter.modeldefinition.ast.ModelASTStagePerModule`.
