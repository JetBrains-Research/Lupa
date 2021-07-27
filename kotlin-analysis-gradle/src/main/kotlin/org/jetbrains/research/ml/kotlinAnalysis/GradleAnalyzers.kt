package org.jetbrains.research.ml.kotlinAnalysis

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
