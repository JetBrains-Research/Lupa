package org.jetbrains.research.ml.kotlinAnalysis.gradle

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
            GradleConstants.DEFAULT_SCRIPT_NAME,
            GradleConstants.KOTLIN_DSL_SCRIPT_NAME
        )

        private val SETTINGS_GRADLE_SCRIPT_NAMES = listOf(
            GradleConstants.SETTINGS_FILE_NAME,
            GradleConstants.KOTLIN_DSL_SETTINGS_FILE_NAME
        )

        /** Converts [PsiFile] to [BuildGradleKtsPsiFile] or [BuildGradleGroovyPsiFile] according to type. */
        private fun toBuildGradlePsiFile(psiFile: PsiFile): BuildGradlePsiFile? {
            return when (psiFile) {
                is GroovyFileImpl -> BuildGradleGroovyPsiFile(psiFile)
                is KtFile -> if (psiFile.isScript()) BuildGradleKtsPsiFile(psiFile) else null
                else -> null
            }
        }

        /** Converts [PsiFile] to [SettingsGradleKtsPsiFile] or [SettingsGradleGroovyPsiFile] according to type. */
        private fun toSettingsGradlePsiFile(psiFile: PsiFile): SettingsGradlePsiFile? {
            return when (psiFile) {
                is GroovyFileImpl -> SettingsGradleGroovyPsiFile(psiFile)
                is KtFile -> if (psiFile.isScript()) SettingsGradleKtsPsiFile(psiFile) else null
                else -> null
            }
        }

        /** Extracts all Gradle files from given [module]. */
        private fun <T> extractGradleFileFromModule(
            module: Module,
            gradleFileNames: List<String>,
            toGradleFile: (PsiFile) -> T?
        ): T? {
            return gradleFileNames
                .mapNotNull { module.findPsiFileByName(it) }
                .mapNotNull { toGradleFile(it) }
                .toList().firstOrNull()
        }

        /** Extracts all Gradle files from given [project]. */
        private fun <T> extractGradleFilesFromProject(
            project: Project,
            gradleFileNames: List<String>,
            toGradleFile: (PsiFile) -> T?
        ): List<T> {
            return project.extractPsiFiles { file -> file.name in gradleFileNames }
                .mapNotNull { toGradleFile(it) }
                .toList()
        }

        /** Extracts all Gradle build files from given [module]. */
        fun extractBuildGradleFileFromModule(module: Module): BuildGradlePsiFile? {
            return extractGradleFileFromModule(
                module,
                BUILD_GRADLE_SCRIPT_NAMES,
                ::toBuildGradlePsiFile
            )
        }

        /** Extracts all Gradle build files from given [project]. */
        fun extractBuildGradleFilesFromProject(project: Project): List<BuildGradlePsiFile> {
            return extractGradleFilesFromProject(
                project,
                BUILD_GRADLE_SCRIPT_NAMES,
                ::toBuildGradlePsiFile
            )
        }

        /** Extracts root Gradle build file from given [project]. */
        fun extractRootBuildGradleFileFromProject(project: Project): BuildGradlePsiFile? {
            return project.findPsiFile { file -> file.name in BUILD_GRADLE_SCRIPT_NAMES }
                ?.run(::toBuildGradlePsiFile)
        }

        /** Extracts all Gradle settings files from given [module]. */
        fun extractSettingsGradleFileFromModule(module: Module): SettingsGradlePsiFile? {
            return extractGradleFileFromModule(
                module,
                SETTINGS_GRADLE_SCRIPT_NAMES,
                ::toSettingsGradlePsiFile
            )
        }

        /** Extracts all Gradle settings files from given [project]. */
        fun extractSettingsGradleFilesFromProject(project: Project): List<SettingsGradlePsiFile> {
            return extractGradleFilesFromProject(
                project,
                SETTINGS_GRADLE_SCRIPT_NAMES,
                ::toSettingsGradlePsiFile
            )
        }
    }
}
