package com.github.takezoe.airspec.intellij

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.codeInspection.collections.isOfClassFrom
import org.jetbrains.plugins.scala.extensions.{ObjectExt, ResolvesTo}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScTemplateDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.ScClassImpl
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestFramework

import AirSpecTestFramework._

class AirSpecTestFramework extends AbstractTestFramework {
  override def getName: String              = "AirSpec"
  override def baseSuitePaths: Seq[String]  = Seq("wvlet.airspec.AirSpec")
  override def getMarkerClassFQName: String = "wvlet.airspec.AirSpec"
  override def getDefaultSuperClass: String = "wvlet.airspec.AirSpec"

  override def isTestMethod(element: PsiElement): Boolean = isTestMethod(element, checkAbstract = false)

  override def isTestMethod(element: PsiElement, checkAbstract: Boolean): Boolean =
    element match {
      case sc: ScReferenceExpression => resolvesToTestMethod(sc)
      case _                         => false
    }

  override protected def isTestClass(definition: ScTemplateDefinition): Boolean =
    if (!definition.is[ScClass]) {
      false
    } else {
      super.isTestClass(definition)
    }

  private def resolvesToTestMethod(sc: ScReferenceExpression): Boolean =
    sc match {
      case ResolvesTo(f: ScFunctionDefinition) =>
        f.returnType match {
          case Right(SpecReturnType(returnType)) if isOfClassFrom(returnType, Seq("wvlet.airspec._")) =>
            expandsToTestMethod(returnType)
          case _ => false
        }
      case _ => false
    }

  object SpecReturnType {
    def unapply(tpe: ScType): Option[ScType] =
      tpe.aliasType match {
        case Some(alias) => alias.upper.toOption
        case None        => Some(tpe)
      }
  }
}

object AirSpecTestFramework {
  private val testMethodTypes = Set(
    "_root_.wvlet.airspec.AirSpecTestBuilder"
  )

  def expandsToTestMethod(tpe: ScType): Boolean =
    tpe.extractClass.collect {
      case c: ScClassImpl =>
        val qname = c.qualifiedName
        val canonical = (if (qname == null || qname == c.name) {
                           c.name
                         } else {
                           "_root_." + qname
                         }) + c.typeParamString
        testMethodTypes.contains(canonical)
    }.getOrElse(false)
}
