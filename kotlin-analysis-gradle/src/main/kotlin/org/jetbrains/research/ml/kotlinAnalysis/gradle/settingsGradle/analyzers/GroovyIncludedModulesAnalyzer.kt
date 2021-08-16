package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers

import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrApplicationStatementImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiMainAnalyzer

/** Groovy settings.gradle file analyzer for included modules. */
object GroovyIncludedModulesAnalyzer : PsiMainAnalyzer<List<String>, List<String>>(
    listOf(SettingsGradleIncludedModulesAnalyzer(GrApplicationStatementImpl::class.java)),
    SettingsGradleIncludedModulesAggregator
)
