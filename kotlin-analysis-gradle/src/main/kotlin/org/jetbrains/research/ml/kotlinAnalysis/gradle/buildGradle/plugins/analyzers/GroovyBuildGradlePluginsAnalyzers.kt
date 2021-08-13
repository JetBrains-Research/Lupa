package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers

import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GroovyBuildGradleBlockContextController
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePlugin

/**
 * Analyser for gradle plugin which parse [BuildGradlePlugin] form [GrApplicationStatementImpl] inside
 * [GradleBlock.PLUGINS] block.
 */
object GroovyBuildGradlePluginAnalyzer :
    BuildGradlePluginAnalyzer<GrApplicationStatementImpl>(GrApplicationStatementImpl::class.java)

/**
 * In Groovy file plugin is stored inside
 * [dependency][GrClosableBlock] block ->
 * [implementation|api|complile|...][GrApplicationStatement] block ->
 * as its arguments.
 */
object GroovyBuildGradlePluginsAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradlePlugin?, Set<BuildGradlePlugin>>(
        listOf(GroovyBuildGradleBlockContextController),
        listOf(GroovyBuildGradlePluginAnalyzer),
        BuildGradlePluginsAggregator
    )
