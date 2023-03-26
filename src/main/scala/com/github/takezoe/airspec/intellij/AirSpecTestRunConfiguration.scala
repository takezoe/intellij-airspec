package com.github.takezoe.airspec.intellij

import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.psi.PsiClass
import com.intellij.testIntegration.TestFramework
import org.jetbrains.plugins.scala.testingSupport.test.testdata.{ClassTestData, TestConfigurationData}
import org.jetbrains.plugins.scala.testingSupport.test.{
  AbstractTestFramework,
  AbstractTestRunConfiguration,
  RunStateProvider,
  SuiteValidityChecker,
  SuiteValidityCheckerBase,
  TestKind
}

import org.jetbrains.plugins.scala.testingSupport.test._

class AirSpecTestRunConfiguration(project: Project, configurationFactory: ConfigurationFactory, name: String)
    extends AbstractTestRunConfiguration(project, configurationFactory, name) {
  self =>

  override def configurationProducer: AirSpecTestRunConfigurationProducer =
    RunConfigurationProducer.EP_NAME.findExtension(classOf[AirSpecTestRunConfigurationProducer])

  override def testFramework: AbstractTestFramework =
    TestFramework.EXTENSION_NAME.findExtension(classOf[AirSpecTestFramework])

  override protected def validityChecker: SuiteValidityChecker = AirSpecTestRunConfiguration.validityChecker

  override def runStateProvider: RunStateProvider =
    new SbtShellBasedStateProvider(this, new AirSpecSbtTestRunningSupport())
}

object AirSpecTestRunConfiguration {
  private val validityChecker =
    new SuiteValidityCheckerBase {
      override protected def isValidClass(clazz: PsiClass): Boolean           = true
      override protected def hasSuitableConstructor(clazz: PsiClass): Boolean = true
    }
}
