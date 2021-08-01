package org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.BuildGradleDependency

/**
 * Controller for stack of blocks in build.gradle.kts file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object KtsBuildGradleBlockContextController :
    GradleBlockContextController<KtCallExpression>(
        KtCallExpression::class.java,
        { psiElement -> psiElement.calleeExpression?.text?.let { GradleBlock.fromSimpleName(it) } })

/**
 * Analyser for gradle dependency which parse [BuildGradleDependency] form [KtCallExpression] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
object KtsBuildGradleDependencyAnalyzer : BuildGradleDependencyAnalyzer<KtCallExpression>(KtCallExpression::class.java)

/**
 * In Kotlin file dependency is stored inside
 * [dependency][KtCallExpression] block ->
 * [implementation|api|compile|...][KtCallExpression] block ->
 * as its arguments
 */
object KtsBuildGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradleDependency?, List<BuildGradleDependency>>(
        listOf(KtsBuildGradleBlockContextController),
        listOf(KtsBuildGradleDependencyAnalyzer),
        BuildGradleDependenciesAggregator
    )
