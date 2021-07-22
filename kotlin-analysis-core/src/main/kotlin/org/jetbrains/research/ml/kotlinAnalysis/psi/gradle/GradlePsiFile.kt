package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import com.intellij.psi.PsiFile

/**
 * Wrapper class for gradle [PsiFile]. Sets the interface for working with build.gradle/build.gradle.kts files
 * content.
 */
sealed class GradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {

    /** Extracts dependency from gradle file by name. */
    fun extractBuildGradleDependencyByName(name: String): GradleDependency? {
        return extractBuildGradleDependencies().firstOrNull { it.group.contains(name) }
    }

    /** Extracts all dependencies from gradle file. */
    abstract fun extractBuildGradleDependencies(): List<GradleDependency>
}
