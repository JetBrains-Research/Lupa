package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import org.jetbrains.kotlin.psi.KtFile

class GradleKtxPsiFile(private val psiFile: KtFile) : GradlePsiFile(psiFile) {

    override fun extractBuildGradleDependencyByName(name: String): GradleDependency? {
        return extractBuildGradleDependencies().firstOrNull { it.name == name }
    }

    override fun extractBuildGradleDependencies(): List<GradleDependency> {
        return psiFile.script?.declarations?.map { dependency ->
            GradleDependency(dependency.text, GradleDependencyConfiguration.IMPLEMENTATION)
        } ?: listOf()
    }
}
