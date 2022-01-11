package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.settingsGradle.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.lupa.kotlinAnalysis.PsiMainAnalyzer

/** Kotlin settings.gradle.kts file analyzer for included modules. */
object KtsIncludedModulesAnalyzer : PsiMainAnalyzer<List<String>, List<String>>(
    listOf(SettingsGradleIncludedModulesAnalyzer(KtCallExpression::class.java)),
    SettingsGradleIncludedModulesAggregator
)
