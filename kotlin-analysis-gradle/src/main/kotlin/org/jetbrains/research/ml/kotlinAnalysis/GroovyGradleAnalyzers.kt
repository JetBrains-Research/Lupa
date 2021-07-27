package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl

/**
 * Controller for stack of blocks in build.gradle file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object GroovyGradleBlockContextController : PsiContextControllerImpl<GradleBlockContext, GrMethodCallExpressionImpl>
    (GrMethodCallExpressionImpl::class.java) {

    private fun getGradleBlock(psiElement: GrMethodCallExpressionImpl): GradleBlock? {
        return psiElement.callReference?.methodName?.let { GradleBlock.fromSimpleName(it) }
    }

    override fun openContext(psiElement: GrMethodCallExpressionImpl, context: GradleBlockContext) {
        getGradleBlock(psiElement)?.let { context.blocksStack.add(it) }
    }

    override fun closeContext(psiElement: GrMethodCallExpressionImpl, context: GradleBlockContext) {
        getGradleBlock(psiElement)?.let {
            assert(context.blocksStack.isNotEmpty()) { "Can not close context due to empty context stack" }
            assert(context.blocksStack.last() == it) { "Can not close context due to invalid context stack" }
            context.blocksStack.removeLast()
        }
    }
}

/**
 * Analyser for gradle dependency which parse [GradleDependency] form [GrApplicationStatementImpl] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
object GroovyGradleDependencyAnalyzer :
    PsiAnalyzerWithContextImpl<GradleBlockContext, GrApplicationStatementImpl, GradleDependency?>
        (GrApplicationStatementImpl::class.java) {

    override fun analyze(psiElement: GrApplicationStatementImpl, context: GradleBlockContext): GradleDependency? {
        return if (context.blocksStack.contains(GradleBlock.DEPENDENCIES)) {
            GradleFileUtil.parseGradleDependencyFromString(psiElement.text)
        } else {
            null
        }
    }
}

/**
 * In Groovy file dependency is stored inside
 * [dependency][GrClosableBlock] block ->
 * [implementation|api|complile|...][GrApplicationStatement] block ->
 * as its arguments.
 */
object GroovyGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>(
        listOf(GroovyGradleBlockContextController),
        listOf(GroovyGradleDependencyAnalyzer),
        GradleDependenciesAggregator
    )
