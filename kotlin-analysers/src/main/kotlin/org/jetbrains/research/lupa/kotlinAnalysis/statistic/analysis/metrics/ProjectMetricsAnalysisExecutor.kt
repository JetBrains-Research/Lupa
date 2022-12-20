package org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.metrics

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.findPsiFilesByExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.util.Extension
import java.nio.file.Path

/**
 * Executor for project metrics analysis which collects number of modules, files,
 * dependencies in all modules' gradle files to csv file with columns:
 * "project_name", "module_name", "files_count", "lines_count", "symbols_count".
 */
class ProjectMetricsAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "project_metrics_data.csv",
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val projectMetricsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "module_name", "files_count", "lines_count", "symbols_count")
            .joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectMetricsDataWriter)

    override val requiredFileExtensions: Set<FileExtension> = emptySet()

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
                        files.sumOf { file -> file.textLength },
                    ).joinToString(separator = ","),
                )
            }
    }
}
