package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.analyzers

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerWithContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorWithContext
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependency
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependencyConfiguration
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.depenencies.BuildGradleDependencyFileUtil

/**
 * Analyser for gradle dependency which parse [BuildGradleDependency] form []psiElement][P] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
open class BuildGradleDependencyAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, GradleBlockContext, BuildGradleDependency?>(pClass) {

    override fun analyzeWithContext(psiElement: P, context: GradleBlockContext?): BuildGradleDependency? {
        assert(context != null) { "Context should be provided" }
        return if (context!!.containsBlock(GradleBlock.DEPENDENCIES)) {
            BuildGradleDependencyFileUtil.parseGradleDependencyParams(psiElement.text)
                ?.let { (configKey, groupId, artifactId) ->
                    BuildGradleDependency(
                        groupId,
                        artifactId,
                        BuildGradleDependencyConfiguration.fromKey(configKey),
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
