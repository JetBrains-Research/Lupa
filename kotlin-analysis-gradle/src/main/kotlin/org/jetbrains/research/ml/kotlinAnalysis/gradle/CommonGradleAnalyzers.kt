package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.*

/** Analysis context which stores stack of visited blocks [GradleBlock] on path to current psi element. */
class GradleBlockContext(private val blocksStack: MutableList<GradleBlock> = mutableListOf()) : AnalyzerContext {

    fun addBlock(block: GradleBlock) {
        blocksStack.add(block)
    }

    fun removeBlock(block: GradleBlock) {
        assert(blocksStack.isNotEmpty()) { "Can not close context due to empty context stack" }
        assert(blocksStack.last() == block) { "Can not close context due to invalid context stack" }
        blocksStack.removeLast()
    }

    fun containsBlock(block: GradleBlock): Boolean {
        return blocksStack.contains(block)
    }
}

/**
 * Controller for stack of blocks in gradle files, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
open class GradleBlockContextController<P : PsiElement>(
    pClass: Class<P>,
    val getGradleBlock: (P) -> GradleBlock?
) : PsiContextControllerImpl<P, GradleBlockContext>(pClass) {

    override fun openContext(psiElement: P, context: GradleBlockContext?) {
        getGradleBlock(psiElement)?.let { context?.addBlock(it) }
    }

    override fun closeContext(psiElement: P, context: GradleBlockContext?) {
        getGradleBlock(psiElement)?.let { context?.removeBlock(it) }
    }
}

/**
 * Analyser for gradle dependency which parse [GradleDependency] form []psiElement][P] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
open class GradleDependencyAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, GradleBlockContext, GradleDependency?>(pClass) {

    override fun analyzeWithContext(psiElement: P, context: GradleBlockContext?): GradleDependency? {
        assert(context != null) { "Context should be provided" }
        return if (context!!.containsBlock(GradleBlock.DEPENDENCIES)) {
            GradleFileUtil.parseGradleDependencyParams(psiElement.text)?.let { (group, name, key) ->
                GradleDependency(
                    group,
                    name,
                    GradleDependencyConfiguration.fromKey(key),
                    context.containsBlock(GradleBlock.ALL_PROJECTS)
                )
            }
        } else {
            null
        }
    }
}

/** Aggregator which combine results from gradle dependency analysis to list of [GradleDependency]. */
object GradleDependenciesAggregator :
    AnalyzersAggregatorWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>() {
    override fun aggregate(analyzerToStat: AnalyzerWithContextToStat<GradleBlockContext, GradleDependency?>):
            List<GradleDependency> {
        return analyzerToStat.values.flatMap { it.values }.filterNotNull().toList()
    }
}
