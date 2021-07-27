package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import java.nio.file.Path

/**
 * Executor for gradle dependencies analysis which collects group name, name and configuration of all
 * dependencies in all gradle files to csv file with columns "project_name", "module_id", "group", "name", "config".
 */
class GradleDependenciesAnalysisExecutor(outputDir: Path, filename: String = "gradle_dependencies_data.csv") :
    AnalysisExecutor() {

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_id", "group", "name", "config")
            .joinToString(separator = ",")
    )
    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)

    override fun analyse(project: Project) {
        val gradleFiles = GradleFileManager.extractGradleFilesFromProject(project)
        gradleFiles.forEachIndexed { gradleFileIndex, gradleFile ->
            val gradleDependencies = gradleFile.extractBuildGradleDependencies()
            gradleDependencies.forEach {
                gradleDependenciesDataWriter.writer.println(
                    listOf(
                        project.name.replace('#', '/'),
                        gradleFileIndex,
                        it.group,
                        it.name,
                        it.configuration?.key ?: "-"
                    ).joinToString(separator = ",")
                )
            }
        }
    }
}
