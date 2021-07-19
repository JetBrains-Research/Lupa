package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPsiFiles
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.findPsiFileByName

class GradleFileManager {

    companion object {
        private val BUILD_GRADLE_KTX_FILE: String = "build.gradle"
        private val BUILD_GRADLE_GROOVY_FILE: String = "build.gradle.ktx"

        private val BUILD_GRADLE_FILES = listOf(
            BUILD_GRADLE_GROOVY_FILE, BUILD_GRADLE_KTX_FILE
        )

        private fun PsiFile.toGradlePsiFile(): GradlePsiFile? {
            return when (this) {
                is KtFile -> GradleKtxPsiFile(this)
                is GroovyFileImpl -> GradleGroovyPsiFile(this)
                else -> null
            }
        }

        fun extractGradleFileFromModule(module: Module): GradlePsiFile? {
            return BUILD_GRADLE_FILES
                .mapNotNull { module.findPsiFileByName(it) }
                .mapNotNull { it.toGradlePsiFile() }
                .toList().firstOrNull()
        }

        fun extractGradleFilesFromProject(project: Project): List<GradlePsiFile> {
            return project.extractPsiFiles { true }.mapNotNull { it.toGradlePsiFile() }.toList()
        }
    }
}
