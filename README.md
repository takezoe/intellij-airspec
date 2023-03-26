# AirSpec support for IntelliJ

IntelliJ plugin for [AirSpec](https://wvlet.org/airframe/docs/airspec)

## Build and Install

Build the plugin artifact by running the following command:

```shell
$ sbt clean package doPatchPluginXml packageArtifactZip
```

Then, you can install `target/intellij-airspec-<version>.zip` from the disk.
