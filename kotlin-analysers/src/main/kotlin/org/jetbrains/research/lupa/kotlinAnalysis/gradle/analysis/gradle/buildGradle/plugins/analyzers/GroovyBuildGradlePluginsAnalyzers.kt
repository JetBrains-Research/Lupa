package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.analyzers

import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.research.lupa.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.context.GroovyBuildGradleBlockContextController
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.plugins.BuildGradlePlugin

/** Analyser for gradle plugin which parse [BuildGradlePlugin] form [GrApplicationStatementImpl]. */
object GroovyBuildGradlePluginAnalyzer :
    BuildGradlePluginAnalyzer<GrApplicationStatementImpl>(GrApplicationStatementImpl::class.java)

/** Analyzer for gradle plugins which extracts all applied or declared plugins in build.gradle file. */
object GroovyBuildGradlePluginsAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradlePlugin?, Set<BuildGradlePlugin>>(
        listOf(GroovyBuildGradleBlockContextController),
        listOf(GroovyBuildGradlePluginAnalyzer),
        BuildGradlePluginsAggregator
    )
