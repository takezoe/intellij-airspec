package com.github.takezoe.airspec.intellij

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}

import javax.swing.Icon

class AirSpecTestConfigurationType extends ConfigurationType {
  val confFactory                                                     = new AirSpecTestRunConfigurationFactory(this)
  override def getDisplayName: String                                 = "AirSpec"
  override def getConfigurationTypeDescription: String                = "AirSpec test run configuration"
  override def getIcon: Icon                                          = AirframeIcon
  override def getId: String                                          = "AirSpecTestRunConfiguration"
  override def getConfigurationFactories: Array[ConfigurationFactory] = Array[ConfigurationFactory](confFactory)
}
