package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties.analyzers.GradlePropertiesAnalyzer

/**
 * Wrapper class for settings gradle [PsiFile]. Sets the interface for working with settings.gradle/settings.gradle.kts
 * files content.
 */
class GradlePropertiesPsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /** Extracts included module names. */
    fun extractGradleProperties(): List<GradleProperty> {
        return GradlePropertiesAnalyzer.analyze(this)
    }
}
