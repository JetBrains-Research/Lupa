package org.jetbrains.research.ml.kotlinAnalysis.analyzers

import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerContext
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerWithContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorWithContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleDependency

/** Analysis context which stores stack of visited blocks [GradleBlock] on path to current psi element. */
data class GradleBlockContext(var blocksStack: MutableList<GradleBlock> = mutableListOf()) : AnalyzerContext

/** Aggregator which combine results from gradle dependency analysis to list of [GradleDependency]. */
object GradleDependenciesAggregator :
    AnalyzersAggregatorWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>() {
    override fun aggregate(analyzerToStat: AnalyzerWithContextToStat<GradleBlockContext, GradleDependency?>):
            List<GradleDependency> {
        return analyzerToStat.values.flatMap { it.values }.filterNotNull().toList()
    }
}
