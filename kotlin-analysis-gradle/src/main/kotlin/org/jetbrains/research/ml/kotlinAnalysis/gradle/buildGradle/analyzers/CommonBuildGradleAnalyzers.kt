package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.*
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.BuildGradleFileUtil
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.BuildGradleDependency
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.BuildGradleDependencyConfiguration

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
 * Analyser for gradle dependency which parse [BuildGradleDependency] form []psiElement][P] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
open class BuildGradleDependencyAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, GradleBlockContext, BuildGradleDependency?>(pClass) {

    override fun analyzeWithContext(psiElement: P, context: GradleBlockContext?): BuildGradleDependency? {
        assert(context != null) { "Context should be provided" }
        return if (context!!.containsBlock(GradleBlock.DEPENDENCIES)) {
            BuildGradleFileUtil.parseGradleDependencyParams(psiElement.text)?.let { (key, group, name) ->
                BuildGradleDependency(
                    group,
                    name,
                    BuildGradleDependencyConfiguration.fromKey(key),
                    context.containsBlock(GradleBlock.ALL_PROJECTS)
                )
            }
        } else {
            null
        }
    }
}

/** Aggregator which combine results from gradle dependency analysis to list of [BuildGradleDependency]. */
object BuildGradleDependenciesAggregator :
    AnalyzersAggregatorWithContext<GradleBlockContext, BuildGradleDependency?, Set<BuildGradleDependency>>() {
    override fun aggregate(analyzerToStat: AnalyzerWithContextToStat<GradleBlockContext, BuildGradleDependency?>):
            Set<BuildGradleDependency> {
        return analyzerToStat.values.flatMap { it.values }.filterNotNull().toHashSet()
    }
}
