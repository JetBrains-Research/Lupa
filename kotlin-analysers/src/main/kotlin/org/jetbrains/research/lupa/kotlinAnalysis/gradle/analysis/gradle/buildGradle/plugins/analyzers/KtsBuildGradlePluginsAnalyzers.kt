package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.lupa.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.context.KtsBuildGradleBlockContextController
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.BuildGradlePlugin

/** Analyser for gradle plugin which parse [BuildGradlePlugin] form [KtCallExpression].*/
object KtsBuildGradlePluginAnalyzer : BuildGradlePluginAnalyzer<KtCallExpression>(KtCallExpression::class.java)

/** Analyzer for gradle plugins which extracts all applied or declared plugins in build.gradle.kts file. */
object KtsBuildGradlePluginsAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradlePlugin?, Set<BuildGradlePlugin>>(
        listOf(KtsBuildGradleBlockContextController),
        listOf(KtsBuildGradlePluginAnalyzer),
        BuildGradlePluginsAggregator
    )
