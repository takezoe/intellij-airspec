package com.github.takezoe.airspec.intellij

import org.jetbrains.plugins.scala.testingSupport.test.sbt.{
  SbtCommandsBuilder,
  SbtCommandsBuilderBase,
  SbtTestRunningSupportBase
}

class AirSpecSbtTestRunningSupport extends SbtTestRunningSupportBase {
  override def commandsBuilder: SbtCommandsBuilder = new SbtCommandsBuilderBase {
    override def testNameKey: Option[String] = Some("-- -t")
  }
}
