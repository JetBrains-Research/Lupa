package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.gradleProperties

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.gradleProperties.analyzers.GradlePropertiesAnalyzer

/** Wrapper class for gradle.properties [PsiFile]. */
class GradlePropertiesPsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /** Extracts list of gradle properties. */
    fun extractGradleProperties(): List<GradleProperty> {
        return ApplicationManager.getApplication().runReadAction<List<GradleProperty>> {
            GradlePropertiesAnalyzer.analyze(this)
        }
    }
}
