package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules.GradleDependenciesCollector
import org.jetbrains.research.ml.kotlinAnalysis.gradle.settingsGradle.modules.ModulesGraph
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractModules
import java.nio.file.Path

/**
 * Executor for gradle dependencies analysis which collects group name, name and configuration of all
 * dependencies in all gradle files to csv file with columns "project_name", "module_id", "group", "name", "config".
 */
class GradleDependenciesAnalysisExecutor(outputDir: Path, filename: String = "gradle_dependencies_data.csv") :
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
            .moduleNameToGradleDependencies
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
