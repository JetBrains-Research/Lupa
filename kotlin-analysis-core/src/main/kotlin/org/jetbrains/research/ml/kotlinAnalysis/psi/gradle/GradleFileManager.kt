package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPsiFiles
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.findPsiFile
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.findPsiFileByName

/**
 * [GradleFileManager] contains methods for extraction Gradle files from project and modules.
 * It supports both Kotlin and Groovy dialects of Gradle.
 */
class GradleFileManager {

    companion object {

        private val BUILD_GRADLE_SCRIPT_NAMES = listOf(
            GradleConstants.DEFAULT_SCRIPT_NAME, GradleConstants.KOTLIN_DSL_SCRIPT_NAME
        )

        /** Converts [PsiFile] to [GradleKtxPsiFile] or [GradleGroovyPsiFile] according to type. */
        private fun PsiFile.toGradlePsiFile(): GradlePsiFile? {
            return when (this) {
                is GroovyFileImpl -> GradleGroovyPsiFile(this)
                is KtFile -> if (this.isScript()) GradleKtxPsiFile(this) else null
                else -> null
            }
        }

        /** Extracts all Gradle files from given [module]. */
        fun extractGradleFileFromModule(module: Module): GradlePsiFile? {
            return BUILD_GRADLE_SCRIPT_NAMES
                .mapNotNull { module.findPsiFileByName(it) }
                .mapNotNull { it.toGradlePsiFile() }
                .toList().firstOrNull()
        }

        /** Extracts all Gradle files from given [project]. */
        fun extractGradleFilesFromProject(project: Project): List<GradlePsiFile> {
            return project.extractPsiFiles { file -> file.name in BUILD_GRADLE_SCRIPT_NAMES }
                .mapNotNull { it.toGradlePsiFile() }
                .toList()
        }

        /** Extracts root Gradle file from given [project]. */
        fun extractRootGradleFileFromProject(project: Project): GradlePsiFile? {
            return project.findPsiFile { file -> file.name in BUILD_GRADLE_SCRIPT_NAMES }
                ?.toGradlePsiFile()
        }
    }
}
