package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers.GroovyIncludedModulesAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.gradle.analyzers.KtsIncludedModulesAnalyzer

/**
 * Wrapper class for settings gradle [PsiFile]. Sets the interface for working with settings.gradle/settings.gradle.kts
 * files content.
 */
sealed class SettingsGradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /** Extracts included module names. */
    abstract fun extractIncludedModuleNames(): List<String>
}

/**
 * Wrap for Gradle settings file written on Kotlin. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class SettingsGradleKtsPsiFile(psiFile: KtFile) : SettingsGradlePsiFile(psiFile) {

    override fun extractIncludedModuleNames(): List<String> {
        return KtsIncludedModulesAnalyzer.analyze(this)
    }
}

/**
 * Wrap for Gradle settings file written on Groovy. Contains specific for it's format methods for extraction elements
 * from build.gradle files.
 */
class SettingsGradleGroovyPsiFile(psiFile: GroovyFileImpl) : SettingsGradlePsiFile(psiFile) {

    override fun extractIncludedModuleNames(): List<String> {
        return GroovyIncludedModulesAnalyzer.analyze(this)
    }
}
