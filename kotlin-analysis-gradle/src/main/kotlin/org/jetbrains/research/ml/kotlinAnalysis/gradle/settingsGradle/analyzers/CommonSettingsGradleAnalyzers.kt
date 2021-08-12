package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerIgnoreContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorIgnoreContext
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerIgnoreContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.SettingsGradleFileUtil

/** Analyser for gradle settings file which parse imported modules names form [psiElement][P]. */
open class SettingsGradleIncludedModulesAnalyzer<P : PsiElement>(pClass: Class<P>) :
    PsiAnalyzerIgnoreContextImpl<P, List<String>>(pClass) {
    override fun analyzeIgnoreContext(psiElement: P): List<String>? {
        return SettingsGradleFileUtil.parseIncludedModulesFromString(psiElement.text)
    }
}

/** Aggregator which combine imported modules names from gradle settings file. */
object SettingsGradleIncludedModulesAggregator :
    AnalyzersAggregatorIgnoreContext<List<String>, List<String>>() {
    override fun aggregateIgnoreContext(analyzerToStat: AnalyzerIgnoreContextToStat<List<String>>): List<String> {
        return analyzerToStat.values.flatMap { it.values }.flatten().toList()
    }
}
