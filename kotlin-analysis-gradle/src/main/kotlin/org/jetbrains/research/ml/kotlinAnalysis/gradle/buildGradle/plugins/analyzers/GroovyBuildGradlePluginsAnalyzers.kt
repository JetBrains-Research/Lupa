package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers

import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GroovyBuildGradleBlockContextController
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePlugin

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
