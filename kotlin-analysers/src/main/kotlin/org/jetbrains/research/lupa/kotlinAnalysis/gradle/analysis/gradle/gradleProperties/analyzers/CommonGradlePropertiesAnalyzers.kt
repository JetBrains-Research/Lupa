package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.gradleProperties.analyzers

import com.intellij.lang.properties.psi.impl.PropertyImpl
import org.jetbrains.research.lupa.kotlinAnalysis.AnalyzerIgnoreContextToStat
import org.jetbrains.research.lupa.kotlinAnalysis.AnalyzersAggregatorIgnoreContext
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzerIgnoreContextImpl
import org.jetbrains.research.lupa.kotlinAnalysis.PsiMainAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.gradleProperties.GradleProperty

/** Analyser for properties in gradle.properties which parse [GradleProperty] form [PropertyImpl]. */
object GradlePropertyAnalyzer : PsiAnalyzerIgnoreContextImpl<PropertyImpl, GradleProperty>(PropertyImpl::class.java) {
    override fun analyzeIgnoreContext(psiElement: PropertyImpl): GradleProperty {
        return GradleProperty(psiElement.key, psiElement.value)
    }
}

/** Aggregator for [GradlePropertyAnalyzer] result which combines [GradleProperty] to list. */
object GradlePropertiesAggregator : AnalyzersAggregatorIgnoreContext<GradleProperty, List<GradleProperty>>() {
    override fun aggregateIgnoreContext(analyzerToStat: AnalyzerIgnoreContextToStat<GradleProperty>):
            List<GradleProperty> {
        return analyzerToStat.values.flatMap { it.values }.toList()
    }
}

/** [PsiMainAnalyzer] implementation which run properties in gradle.properties file analysis. */
object GradlePropertiesAnalyzer : PsiMainAnalyzer<GradleProperty, List<GradleProperty>>(
    listOf(GradlePropertyAnalyzer),
    GradlePropertiesAggregator
)
