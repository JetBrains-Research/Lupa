package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager.Companion.toBuildGradlePsiFile
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
            GradleConstants.DEFAULT_SCRIPT_NAME,
            GradleConstants.KOTLIN_DSL_SCRIPT_NAME
        )

        private val SETTINGS_GRADLE_SCRIPT_NAMES = listOf(
            GradleConstants.SETTINGS_FILE_NAME,
            GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME
        )

        /** Converts [PsiFile] to [BuildGradleKtsPsiFile] or [BuildGradleGroovyPsiFile] according to type. */
        private fun PsiFile.toBuildGradlePsiFile(): BuildGradlePsiFile? {
            return when (this) {
                is GroovyFileImpl -> BuildGradleGroovyPsiFile(this)
                is KtFile -> if (this.isScript()) BuildGradleKtsPsiFile(this) else null
                else -> null
            }
        }

        /** Converts [PsiFile] to [SettingsGradleKtsPsiFile] or [SettingsGradleGroovyPsiFile] according to type. */
        private fun PsiFile.toSettingsGradlePsiFile(): SettingsGradlePsiFile? {
            return when (this) {
                is GroovyFileImpl -> SettingsGradleGroovyPsiFile(this)
                is KtFile -> if (this.isScript()) SettingsGradleKtsPsiFile(this) else null
                else -> null
            }
        }

        /** Extracts all build Gradle files from given [module]. */
        fun extractBuildGradleFileFromModule(module: Module): BuildGradlePsiFile? {
            return BUILD_GRADLE_SCRIPT_NAMES
                .mapNotNull { module.findPsiFileByName(it) }
                .mapNotNull { it.toBuildGradlePsiFile() }
                .toList().firstOrNull()
        }

        /** Extracts all settings Gradle files from given [module]. */
        fun extractSettingsGradleFileFromModule(module: Module): SettingsGradlePsiFile? {
            return SETTINGS_GRADLE_SCRIPT_NAMES
                .mapNotNull { module.findPsiFileByName(it) }
                .mapNotNull { it.toSettingsGradlePsiFile() }
                .toList().firstOrNull()
        }

        /** Extracts all Gradle files from given [project]. */
        fun extractGradleFilesFromProject(project: Project): List<BuildGradlePsiFile> {
            return project.extractPsiFiles { file -> file.name in BUILD_GRADLE_SCRIPT_NAMES }
                .mapNotNull { it.toBuildGradlePsiFile() }
                .toList()
        }

        /** Extracts root Gradle file from given [project]. */
        fun extractRootGradleFileFromProject(project: Project): BuildGradlePsiFile? {
            return project.findPsiFile { file -> file.name in BUILD_GRADLE_SCRIPT_NAMES }
                ?.toBuildGradlePsiFile()
        }
    }
}
