package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import java.nio.file.Path

/**
 * Executor for gradle plugins analysis which collects plugin id of all plugins in all gradle files to csv file with
 * columns: "project_name", "plugin_id".
 */
class GradlePluginsAnalysisExecutor(outputDir: Path, filename: String = "gradle_plugins_data.csv") :
    AnalysisExecutor() {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "plugin_id").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)

    override fun analyse(project: Project) {
        val gradleFiles = GradleFileManager.extractBuildGradleFilesFromProject(project)
        gradleFiles.forEach { gradleFile ->
            val gradlePlugins = gradleFile.extractBuildGradlePlugins()
            gradlePlugins.forEach {
                gradleDependenciesDataWriter.writer.println(
                    listOf(
                        project.name,
                        it.pluginId
                    ).joinToString(separator = ",")
                )
            }
        }
    }
}