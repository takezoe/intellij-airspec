package com.github.takezoe.airspec.intellij

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestRunConfigurationFactory

class AirSpecTestRunConfigurationFactory(configurationType: AirSpecTestConfigurationType)
    extends AbstractTestRunConfigurationFactory(configurationType) {

  override def id: String = "AirSpec"
  override def createTemplateConfiguration(project: Project): RunConfiguration =
    new AirSpecTestRunConfiguration(project, this, "")
}
