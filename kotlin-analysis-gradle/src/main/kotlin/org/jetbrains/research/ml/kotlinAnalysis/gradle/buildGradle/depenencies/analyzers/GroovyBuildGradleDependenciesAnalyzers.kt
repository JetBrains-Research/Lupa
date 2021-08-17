package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.analyzers

import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GroovyBuildGradleBlockContextController
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependency

/**
 * Analyser for gradle dependency which parse [BuildGradleDependency] form [GrApplicationStatementImpl] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
object GroovyBuildGradleDependencyAnalyzer :
    BuildGradleDependencyAnalyzer<GrApplicationStatementImpl>(GrApplicationStatementImpl::class.java)

/**
 * In Groovy file dependency is stored inside
 * [dependency][GrClosableBlock] block ->
 * [implementation|api|complile|...][GrApplicationStatement] block ->
 * as its arguments.
 */
object GroovyBuildGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, BuildGradleDependency?, Set<BuildGradleDependency>>(
        listOf(GroovyBuildGradleBlockContextController),
        listOf(GroovyBuildGradleDependencyAnalyzer),
        BuildGradleDependenciesAggregator
    )
