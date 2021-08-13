package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context

import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerContext

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
