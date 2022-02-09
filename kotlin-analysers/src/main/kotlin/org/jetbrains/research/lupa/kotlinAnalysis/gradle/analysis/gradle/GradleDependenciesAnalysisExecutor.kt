package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.settingsGradle.modules.GradleDependenciesCollector
import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.settingsGradle.modules.ModulesGraph
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

/**
 * Executor for gradle dependencies analysis which collects group id, artifact id and configuration of all
 * dependencies in all modules' gradle files to csv file with columns:
 * "project_name", "module_name", "group_id", "artifact_id", "config".
 */
class GradleDependenciesByModulesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "gradle_dependencies_by_modules_data.csv"
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_name", "group_id", "artifact_id", "config")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = emptySet()

    override fun analyse(project: Project) {
        val graph = ModulesGraph(project.extractModules())
        val gradleDependenciesCollector = GradleDependenciesCollector()
        graph.accept(gradleDependenciesCollector)
        gradleDependenciesCollector
            .getModuleNameToGradleDependencies()
            .forEach { (moduleName, dependencies) ->
                dependencies.forEach {
                    gradleDependenciesDataWriter.writer.println(
                        listOf(
                            project.name.replace('#', '/'),
                            moduleName,
                            it.groupId,
                            it.artifactId,
                            it.configuration?.key ?: "-"
                        ).joinToString(separator = ",")
                    )
                }
            }
    }
}

/**
 * Executor for gradle dependencies analysis which collects group name, name, configuration and version of all
 * dependencies in all gradle files to csv file with columns:
 * "project_name", "group_id", "artifact_id", "config", "version".
 */
class GradleDependenciesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "gradle_dependencies_data.csv"
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "group_id", "artifact_id", "config", "version")
            .joinToString(separator = ",")
    )

    override val requiredFileExtensions: Set<FileExtension> = setOf(FileExtension.GRADLE, FileExtension.KTS)

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)

    override fun analyse(project: Project) {
        val gradleFiles = GradleFileManager.extractBuildGradleFilesFromProject(project)
        gradleFiles.forEach { gradleFile ->
            val gradleDependencies = gradleFile.extractBuildGradleDependencies()
            gradleDependencies.forEach {
                gradleDependenciesDataWriter.writer.println(
                    listOf(
                        project.name,
                        it.groupId,
                        it.artifactId,
                        it.configuration?.key ?: "-",
                        it.version ?: "-"
                    ).joinToString(separator = ",")
                )
            }
        }
    }
}
