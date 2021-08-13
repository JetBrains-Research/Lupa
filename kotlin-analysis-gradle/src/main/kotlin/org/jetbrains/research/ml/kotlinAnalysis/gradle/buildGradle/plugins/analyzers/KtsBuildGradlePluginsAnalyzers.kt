package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.KtsBuildGradleBlockContextController
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePlugin

/**
 * Analyser for gradle plugin which parse [BuildGradlePlugin] form [KtCallExpression] inside
 * [GradleBlock.PLUGIN] block.
 */
object KtsBuildGradlePluginAnalyzer : BuildGradlePluginAnalyzer<KtCallExpression>(KtCallExpression::class.java)

/**
 * In Kotlin file plugins area stored inside
 * [dependency][KtCallExpression] block ->
 * [implementation|api|compile|...][KtCallExpression] block ->
 * as its arguments
 */
object KtsBuildGradlePluginsAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradlePlugin?, Set<BuildGradlePlugin>>(
        listOf(KtsBuildGradleBlockContextController),
        listOf(KtsBuildGradlePluginAnalyzer),
        BuildGradlePluginsAggregator
    )
