package org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers

import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzerWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.BuildGradleDependency

/**
 * Controller for stack of blocks in build.gradle file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object GroovyBuildGradleBlockContextController :
    GradleBlockContextController<GrMethodCallExpressionImpl>(GrMethodCallExpressionImpl::class.java, { psiElement ->
        psiElement.callReference?.methodName?.let { GradleBlock.fromSimpleName(it) }
    })

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
