package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzer

/** */
object KtsIncludedModulesAnalyzer :
    PsiMainAnalyzer<List<String>, List<String>>(
        listOf(SettingsGradleIncludedModulesAnalyzer(KtCallExpression::class.java)),
        SettingsGradleIncludedModulesAggregator
    )
