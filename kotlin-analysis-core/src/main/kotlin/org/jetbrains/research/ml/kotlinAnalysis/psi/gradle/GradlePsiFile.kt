package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import com.intellij.psi.PsiFile

sealed class GradlePsiFile(psiFile: PsiFile) : PsiFile by psiFile {
    val GRADLE_DEPENDENCIES_BLOCK_NAME = "dependencies"
    val GRADLE_PLUGINS_BLOCK_NAME = "plugins"

    abstract fun extractBuildGradleDependencyByName(name: String): GradleDependency?
    abstract fun extractBuildGradleDependencies(): List<GradleDependency>
}
