package org.jetbrains.research.ml.kotlinAnalysis.metrics

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
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
 * "project_name", "module_name", "files_count", "lines_count", "symbols_count".
 */
class ProjectMetricsAnalysisExecutor(
    outputDir: Path,
    filename: String = "project_metrics_data.csv"
) :
    AnalysisExecutor() {

    private val projectMetricsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_name", "files_count", "lines_count", "symbols_count")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectMetricsDataWriter)

    override fun analyse(project: Project) {
        val documentManager = PsiDocumentManager.getInstance(project)
        project.extractModules()
            .forEach { module ->
                val files = module.findPsiFilesByExtension(Extension.KT.value)
                projectMetricsDataWriter.writer.println(
                    listOf(
                        project.name,
                        module.name,
                        files.size,
                        files.sumOf { file -> documentManager.getDocument(file)?.lineCount ?: 0 },
                        files.sumOf { file -> file.textLength }
                    ).joinToString(separator = ",")
                )
            }
    }
}
