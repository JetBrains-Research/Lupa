package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.analyzers

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerWithContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorWithContext
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.context.GradleBlockContext
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePlugin
import org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins.BuildGradlePluginFileUtil

/**
 * Analyser for gradle plugin which parse declared plugins [BuildGradlePlugin] form [psiElement][P] inside
 * [GradleBlock.PLUGINS] block or applied plugins.
 */
open class BuildGradlePluginAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, GradleBlockContext, BuildGradlePlugin?>(pClass) {

    override fun analyzeWithContext(psiElement: P, context: GradleBlockContext?): BuildGradlePlugin? {
        assert(context != null) { "Context should be provided" }
        return if (context!!.containsBlock(GradleBlock.PLUGINS)) {
            BuildGradlePluginFileUtil.parseGradlePluginParams(psiElement.text)
        } else {
            BuildGradlePluginFileUtil.parseGradleApplyPluginParams(psiElement.text)
        }?.let { pluginId ->
            BuildGradlePlugin(
                pluginId,
                context.containsBlock(GradleBlock.ALL_PROJECTS)
            )
        }
    }
}

/** Aggregator which combine results from gradle plugin analysis to list of [BuildGradlePlugin]. */
object BuildGradlePluginsAggregator :
    AnalyzersAggregatorWithContext<GradleBlockContext, BuildGradlePlugin?, Set<BuildGradlePlugin>>() {
    override fun aggregate(analyzerToStat: AnalyzerWithContextToStat<GradleBlockContext, BuildGradlePlugin?>):
            Set<BuildGradlePlugin> {
        return analyzerToStat.values.flatMap { it.values }.filterNotNull().toHashSet()
    }
}
