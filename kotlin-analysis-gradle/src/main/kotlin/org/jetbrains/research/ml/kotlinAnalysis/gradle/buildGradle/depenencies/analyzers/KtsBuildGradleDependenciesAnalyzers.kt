package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.KtsBuildGradleBlockContextController
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependency

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
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradleDependency?, Set<BuildGradleDependency>>(
        listOf(KtsBuildGradleBlockContextController),
        listOf(KtsBuildGradleDependencyAnalyzer),
        BuildGradleDependenciesAggregator
    )
