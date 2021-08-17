package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiContextControllerImpl

/**
 * Controller for stack of blocks in gradle files, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
open class GradleBlockContextController<P : PsiElement>(
    pClass: Class<P>,
    val getGradleBlock: (P) -> GradleBlock?
) : PsiContextControllerImpl<P, GradleBlockContext>(pClass) {

    override fun openContext(psiElement: P, context: GradleBlockContext?) {
        getGradleBlock(psiElement)?.let { context?.addBlock(it) }
    }

    override fun closeContext(psiElement: P, context: GradleBlockContext?) {
        getGradleBlock(psiElement)?.let { context?.removeBlock(it) }
    }
}

/**
 * Controller for stack of blocks in build.gradle.kts file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object KtsBuildGradleBlockContextController :
    GradleBlockContextController<KtCallExpression>(
        KtCallExpression::class.java,
        { psiElement -> psiElement.calleeExpression?.text?.let { GradleBlock.fromSimpleName(it) } })

/**
 * Controller for stack of blocks in build.gradle file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object GroovyBuildGradleBlockContextController :
    GradleBlockContextController<GrMethodCallExpressionImpl>(GrMethodCallExpressionImpl::class.java, { psiElement ->
        psiElement.callReference?.methodName?.let { GradleBlock.fromSimpleName(it) }
    })
