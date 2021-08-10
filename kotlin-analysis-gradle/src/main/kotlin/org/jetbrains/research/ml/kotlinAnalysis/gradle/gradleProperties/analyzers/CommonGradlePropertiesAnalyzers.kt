package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties.analyzers

import com.intellij.lang.properties.psi.impl.PropertyImpl
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerIgnoreContextToStat
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzersAggregatorIgnoreContext
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerIgnoreContextImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties.GradleProperty


object GradlePropertyAnalyzer : PsiAnalyzerIgnoreContextImpl<PropertyImpl, GradleProperty>(PropertyImpl::class.java) {
    override fun analyzeIgnoreContext(psiElement: PropertyImpl): GradleProperty {
        return GradleProperty(psiElement.key, psiElement.value)
    }
}

object GradlePropertiesAggregator : AnalyzersAggregatorIgnoreContext<GradleProperty, List<GradleProperty>>() {
    override fun aggregateIgnoreContext(analyzerToStat: AnalyzerIgnoreContextToStat<GradleProperty>): List<GradleProperty> {
        return analyzerToStat.values.flatMap { it.values }.toList()
    }
}

object GradlePropertiesAnalyzer : PsiMainAnalyzer<GradleProperty, List<GradleProperty>>(
    listOf(GradlePropertyAnalyzer),
    GradlePropertiesAggregator
)

