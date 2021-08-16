package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules.GradleDependenciesCollector
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules.ModulesGraph
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractModules
import java.nio.file.Path

/**
 * Executor for gradle dependencies analysis which collects group id, artifact id and configuration of all
 * dependencies in all modules' gradle files to csv file with columns:
 * "project_name", "module_name", "group_id", "artifact_id", "config".
 */
class GradleDependenciesByModulesAnalysisExecutor(
    outputDir: Path,
    filename: String = "gradle_dependencies_by_modules_data.csv"
) :
    AnalysisExecutor() {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_name", "group_id", "artifact_id", "config")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)

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
 * Executor for gradle dependencies analysis which collects group name, name and configuration of all
 * dependencies in all gradle files to csv file with columns:
 * "project_name", "group_id", "artifact_id", "config".
 */
class GradleDependenciesAnalysisExecutor(outputDir: Path, filename: String = "gradle_dependencies_data.csv") :
    AnalysisExecutor() {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "group_id", "artifact_id", "config")
            .joinToString(separator = ",")
    )

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
                        it.configuration?.key ?: "-"
                    ).joinToString(separator = ",")
                )
            }
        }
    }
}
