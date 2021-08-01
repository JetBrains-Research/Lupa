package org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerIgnoreContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorIgnoreContext
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerIgnoreContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleBlock
import org.jetbrains.research.ml.kotlinAnalysis.gradle.BuildGradleDependency
import org.jetbrains.research.ml.kotlinAnalysis.gradle.SettingsGradleFileUtil

/**
 * Analyser for gradle dependency which parse [BuildGradleDependency] form []psiElement][P] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
open class SettingsGradleIncludedModulesAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerIgnoreContextImpl<P, List<String>>(pClass) {
    override fun analyzeIgnoreContext(psiElement: P): List<String>? {
        return SettingsGradleFileUtil.parseIncludedModulesFromString(psiElement.text)
    }
}


/** Aggregator which combine results from gradle dependency analysis to list of [BuildGradleDependency]. */
object SettingsGradleIncludedModulesAggregator :
    AnalyzersAggregatorIgnoreContext<List<String>, List<String>>() {
    override fun aggregateIgnoreContext(analyzerToStat: AnalyzerIgnoreContextToStat<List<String>>): List<String> {
        return analyzerToStat.values.flatMap { it.values }.flatten().toList()
    }
}
