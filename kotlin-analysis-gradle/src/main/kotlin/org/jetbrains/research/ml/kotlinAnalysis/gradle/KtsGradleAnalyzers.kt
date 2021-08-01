package org.jetbrains.research.ml.kotlinAnalysis.gradle

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext

/**
 * Controller for stack of blocks in build.gradle.kts file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object KtsGradleBlockContextController :
    GradleBlockContextController<KtCallExpression>(
        KtCallExpression::class.java,
        { psiElement -> psiElement.calleeExpression?.text?.let { GradleBlock.fromSimpleName(it) } })

/**
 * Analyser for gradle dependency which parse [GradleDependency] form [KtCallExpression] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
object KtsGradleDependencyAnalyzer : GradleDependencyAnalyzer<KtCallExpression>(KtCallExpression::class.java)

/**
 * In Kotlin file dependency is stored inside
 * [dependency][KtCallExpression] block ->
 * [implementation|api|compile|...][KtCallExpression] block ->
 * as its arguments
 */
object KtsGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>(
        listOf(KtsGradleBlockContextController),
        listOf(KtsGradleDependencyAnalyzer),
        GradleDependenciesAggregator
    )
