package org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers.GroovyIncludedModulesAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.analyzers.KtsIncludedModulesAnalyzer

/**
 * Wrapper class for gradle settings [file][PsiFile]. Sets the interface for working with settings.gradle/settings.gradle.kts
 * files content.
 */
sealed class SettingsGradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /** Extracts included module names. */
    abstract fun extractIncludedModuleNames(): List<String>
}

/** Wrap for Gradle settings file written on Kotlin. */
class SettingsGradleKtsPsiFile(psiFile: KtFile) : SettingsGradlePsiFile(psiFile) {

    override fun extractIncludedModuleNames(): List<String> {
        return KtsIncludedModulesAnalyzer.analyze(this)
    }
}

/** Wrap for Gradle settings file written on Groovy. */
class SettingsGradleGroovyPsiFile(psiFile: GroovyFileImpl) : SettingsGradlePsiFile(psiFile) {

    override fun extractIncludedModuleNames(): List<String> {
        return GroovyIncludedModulesAnalyzer.analyze(this)
    }
}
