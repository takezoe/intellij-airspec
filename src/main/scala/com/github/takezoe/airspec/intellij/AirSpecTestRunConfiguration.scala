package com.github.takezoe.airspec.intellij

import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.{ConfigurationFactory, JavaParameters, RunProfileState}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.testIntegration.TestFramework
import org.jetbrains.plugins.scala.testingSupport.test.CustomTestRunnerBasedStateProvider.TestFrameworkRunnerInfo
import org.jetbrains.plugins.scala.testingSupport.test.{AbstractTestFramework, AbstractTestRunConfiguration, RunStateProvider, SuiteValidityChecker, SuiteValidityCheckerBase}
import org.jetbrains.plugins.scala.testingSupport.test._
import org.jetbrains.plugins.scala.testingSupport.test.sbt.SbtTestRunningSupport
import wvlet.airspec.AirSpecIntelliJRunner

class AirSpecTestRunConfiguration(project: Project, configurationFactory: ConfigurationFactory, name: String)
    extends AbstractTestRunConfiguration(project, configurationFactory, name) {
  self =>

  override def configurationProducer: AirSpecTestRunConfigurationProducer =
    RunConfigurationProducer.EP_NAME.findExtension(classOf[AirSpecTestRunConfigurationProducer])

  override def testFramework: AbstractTestFramework =
    TestFramework.EXTENSION_NAME.findExtension(classOf[AirSpecTestFramework])

  override protected def validityChecker: SuiteValidityChecker = AirSpecTestRunConfiguration.validityChecker

  override def runStateProvider: RunStateProvider =
    new CustomTestRunnerOrSbtShellStateProvider(
      this,
      TestFrameworkRunnerInfo(classOf[AirSpecIntelliJRunner]),
      new AirSpecSbtTestRunningSupport
    )
}

final class CustomTestRunnerOrSbtShellStateProvider(
  configuration: AbstractTestRunConfiguration,
  runnerInfo: TestFrameworkRunnerInfo,
  val sbtSupport: SbtTestRunningSupport
) extends RunStateProvider {

  override def commandLineState(
    env: ExecutionEnvironment,
    failedTests: Option[Seq[(String, String)]]
  ): RunProfileState = {
    val provider =
      if (configuration.testConfigurationData.useSbt)
        new SbtShellBasedStateProvider(configuration, sbtSupport)
      else
        new CustomTestRunnerBasedStateProvider(configuration, runnerInfo)
    provider.commandLineState(env, failedTests)
  }
}

final class CustomTestRunnerBasedStateProvider(
  configuration: AbstractTestRunConfiguration,
  runnerInfo: TestFrameworkRunnerInfo
) extends RunStateProvider {

  override def commandLineState(
    env: ExecutionEnvironment,
    failedTests: Option[Seq[(String, String)]]
  ): RunProfileState =
    new ScalaTestFrameworkCommandLineState(configuration, env, failedTests, runnerInfo) {
      override def getJavaParameters: JavaParameters = {
        val params = super.getJavaParameters
        params.getClassPath.add(runnersJar)
        params
      }
    }
}

object AirSpecTestRunConfiguration {
  private val validityChecker =
    new SuiteValidityCheckerBase {
      override protected def isValidClass(clazz: PsiClass): Boolean           = true
      override protected def hasSuitableConstructor(clazz: PsiClass): Boolean = true
    }
}
