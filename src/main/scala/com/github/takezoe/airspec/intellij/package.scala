package com.github.takezoe.airspec

import com.intellij.codeInsight.TestFrameworks
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.{PsiAnnotation, PsiClass, PsiElement}
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testIntegration.TestFramework
import com.intellij.util.PathUtil
import org.jetbrains.plugins.scala.codeInspection.collections._
import org.jetbrains.plugins.scala.extensions.{PsiClassExt, PsiElementExt}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.api.base.{ScLiteral, ScReference}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunctionDefinition, ScPatternDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTypeDefinition
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.lang.refactoring.util.ScalaNamesUtil

import java.io.File
import scala.jdk.CollectionConverters.CollectionHasAsScala

package object intellij {

  val AirframeIcon = IconLoader.getIcon("/icons/airframe.png", getClass)

  private val runnersJarName = "runners.jar"
  private val libRoot: File = {
    val jarPath = new File(PathUtil.getJarPathForClass(this.getClass)) // scalaCommunity.jar
    jarPath.getParentFile
  }

  val runnersJar = new File(libRoot, runnersJarName)

  def parentTypeDefinition(e: PsiElement): Option[ScTypeDefinition] =
    parentOfType(e, classOf[ScTypeDefinition], strict = false)

  def detectAirSpecTestFramework(c: PsiClass): Option[TestFramework] =
    Option(TestFrameworks.detectFramework(c)) collect {
      case framework: AirSpecTestFramework if framework.isTestClass(c) => framework
    }

  private def parentOfType[T <: PsiElement](
    elem: PsiElement,
    parentClass: Class[T],
    strict: Boolean = true
  ): Option[T] =
    Option(PsiTreeUtil.getParentOfType(elem, parentClass, strict))

  object testName {
    def unapply(expr: ScReferenceExpression): Option[String] =
      expr.parent match {
        case Some(m: ScMethodCall) =>
          m.argumentExpressions.headOption.flatMap {
            case lit: ScLiteral => Option(lit.getValue()).map(_.toString)
            case _              => None
          }
        case _ => None
      }
  }

  object IsAirSpecTestElement {
    def unapply(element: PsiElement): Option[(ScTypeDefinition, Option[ScReferenceExpression])] =
      element match {
        case leaf: LeafPsiElement if leaf.getElementType == ScalaTokenTypes.tIDENTIFIER =>
          leaf.parent match {
            case Some(td: ScTypeDefinition)       => infoForClass(td)
            case Some(ref: ScReferenceExpression) => infoForLocalExpr(ref) // orElse infoForExprFromAnotherClass(ref)
            case _                                => None
          }
        case _ => None
      }

    private def infoForClass(td: ScTypeDefinition) =
      detectAirSpecTestFramework(td).map(_ => (td, None))

    private def infoForReferencedExpr(reference: ScReference, referencedExpr: ScReferenceExpression) =
      for {
        td  <- parentTypeDefinition(reference)
        fw  <- detectAirSpecTestFramework(td)
        ref <- Some(referencedExpr).filter(fw.isTestMethod)
      } yield (td, Some(ref))

    private def infoForLocalExpr(expr: ScReferenceExpression) =
      infoForReferencedExpr(expr, expr)

    // TODO support this when we figure out how to cache this information
    // without performing additional expensive lookups on every element
    private def infoForExprFromAnotherClass(expr: ScReferenceExpression) = {
      val refs = referencesOfPatternDef(expr)
        .orElse(referencesOfFunctionDef(expr))
        .getOrElse(Seq.empty)

      refs.flatMap {
        case ref: ScReferenceExpression => infoForReferencedExpr(ref.getElement, expr)
        case _                          => None
      }.headOption
    }

    private def findAllReferences(elem: PsiElement) =
      ReferencesSearch.search(elem, elem.getUseScope).findAll().asScala

    private def referencesOfPatternDef(elem: PsiElement) =
      parentOfType(elem, classOf[ScPatternDefinition])
        .map(_.bindings.flatMap(findAllReferences))

    private def referencesOfFunctionDef(elem: PsiElement) =
      parentOfType(elem, classOf[ScFunctionDefinition])
        .map(findAllReferences)
  }

  def isOfClassFrom(`type`: ScType, patterns: Seq[String]): Boolean = {
    val typeExtracted = `type`.tryExtractDesignatorSingleton
    isOfClassFromForExtractedType(typeExtracted, patterns)
  }

  private def isOfClassFromForExtractedType(typeExtracted: ScType, patterns: Seq[String]): Boolean = {
    val clazz = typeExtracted.extractClass
    clazz.exists(qualifiedNameFitToPatterns(_, patterns))
  }

  private def qualifiedNameFitToPatterns(clazz: PsiClass, patterns: Seq[String]) =
    Option(clazz).flatMap(c => Option(c.qualifiedName))
      .exists(ScalaNamesUtil.nameFitToPatterns(_, patterns, strict = false))
}
