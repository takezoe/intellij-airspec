package com.github.takezoe.airspec.intellij

import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationTypeUtil}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.lang.psi.api.base.literals.ScStringLiteral
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestConfigurationProducer
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestConfigurationProducer.CreateFromContextInfo
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestConfigurationProducer.CreateFromContextInfo.{
  AllInPackage,
  ClassWithTestName
}

class AirSpecTestRunConfigurationProducer extends AbstractTestConfigurationProducer[AirSpecTestRunConfiguration] {

  override def getConfigurationFactory: ConfigurationFactory = {
    val configurationType = ConfigurationTypeUtil.findConfigurationType(classOf[AirSpecTestConfigurationType])
    configurationType.confFactory
  }

  override protected def suitePaths: Seq[String] = Seq("wvlet.airspec.AirSpec")

  override protected def configurationName(
    contextInfo: AbstractTestConfigurationProducer.CreateFromContextInfo
  ): String =
    contextInfo match {
      case AllInPackage(_, packageName) =>
        s"AirSpec spec in $packageName"
      case ClassWithTestName(testClass, testName) =>
        StringUtil.getShortName(testClass.qualifiedName) + testName.fold("")("::" + _)
    }

  override def getTestClassWithTestName(location: PsiElementLocation): Option[CreateFromContextInfo.ClassWithTestName] =
    location.getPsiElement match {
      case IsAirSpecTestElement(td, tm) =>
        tm match {
          case Some(testName(name)) => Some(ClassWithTestName(td, Some(getTestClassName(location.getPsiElement))))
          case _                    => Some(ClassWithTestName(td, None))
        }
      case _ => None
    }

  private def getTestClassName(e: PsiElement): String = {
    val names = getParentMethodCall(e).flatMap { m =>
      m.argumentExpressions.headOption match {
        case Some(s: ScStringLiteral) => Some(s.getValue())
        case _                        => None
      }
    }
    names.reverse.mkString("/")
  }

  private def getParentMethodCall(e: PsiElement): Seq[ScMethodCall] = {
    val methodCalls = e.getChildren.collect {
      case m: ScMethodCall if m.`type`().exists(AirSpecTestFramework.expandsToTestMethod) => m
    }
    val parent = e.getParent
    if (parent == null || parent.isInstanceOf[ScClass]) {
      methodCalls.toSeq
    } else {
      methodCalls.toSeq ++ getParentMethodCall(parent)
    }
  }
}
