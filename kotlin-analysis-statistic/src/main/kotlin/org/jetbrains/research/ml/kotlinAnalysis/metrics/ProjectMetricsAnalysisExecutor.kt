package org.jetbrains.research.ml.kotlinAnalysis.metrics

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.findPsiFilesByExtension
import org.jetbrains.research.pluginUtilities.util.Extension
import java.nio.file.Path

/**
 * Executor for project metrics analysis which collects number of modules, files,
 * dependencies in all modules' gradle files to csv file with columns:
 * "project_name", "module_name", "group_id", "artifact_id", "config".
 */
class ProjectMetricsAnalysisExecutor(
    outputDir: Path,
    filename: String = "project_metrics_data.csv"
) :
    AnalysisExecutor() {

    private val projectMetricsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_name", "files_count", "total_lines_count", "avg_lines_count")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectMetricsDataWriter)

    override fun analyse(project: Project) {
        val modules = project.extractModules()
        modules.forEach {
            it.findPsiFilesByExtension(Extension.KT.value).map {

            }
        }
        val gradleDependenciesCollector = GradleDependenciesCollector()
        graph.accept(gradleDependenciesCollector)
        gradleDependenciesCollector
            .getModuleNameToGradleDependencies()
            .forEach { (moduleName, dependencies) ->
                dependencies.forEach {
                    projectMetricsDataWriter.writer.println(
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
