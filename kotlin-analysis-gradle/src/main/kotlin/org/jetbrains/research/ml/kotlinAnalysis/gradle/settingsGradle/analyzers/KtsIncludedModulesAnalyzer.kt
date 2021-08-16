package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzer

/** Kotlin settings.gradle.kts file analyzer for included modules. */
object KtsIncludedModulesAnalyzer : PsiMainAnalyzer<List<String>, List<String>>(
    listOf(SettingsGradleIncludedModulesAnalyzer(KtCallExpression::class.java)),
    SettingsGradleIncludedModulesAggregator
)
