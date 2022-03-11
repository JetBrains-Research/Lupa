package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.util.io.delete
import org.jetbrains.research.lupa.kotlinAnalysis.util.*
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Abstract class for analysis orchestrator that provides an interface that allows
 * to filter project files by the extension needed for analysis.
 */
abstract class Orchestrator(protected open val executorHelper: ExecutorHelper? = null) {
    abstract fun doAnalysis(projectPath: Path, projectTmpFolderPath: Path): Boolean

    fun execute(projectsDir: Path, outputDir: Path) {
        val tempFolderPath = Paths.get(outputDir.toString(), "tmp")
        tempFolderPath.delete(recursively = true)

        getSubdirectories(projectsDir).forEachIndexed { projectIndex, projectPath ->
            println("Start analyzing project ${projectPath.fileName} (index=$projectIndex)")
            val isSuccessful = doAnalysis(projectPath, tempFolderPath.resolve(projectPath.fileName))
            println("Project ${projectPath.fileName} (index=$projectIndex) " +
                    "was analyzed successfully: $isSuccessful")
            if (isSuccessful) {
                executorHelper?.postExecuteAction(GitRepository(projectPath))
            }
        }

        tempFolderPath.delete()
    }
}

/**
 * Class for execution analyzer for all projects in given directory.
 */
class AnalysisOrchestrator(
    private val analysisExecutor: AnalysisExecutor,
    executorHelper: ExecutorHelper? = null
) : Orchestrator(executorHelper) {

    override fun doAnalysis(projectPath: Path, projectTmpFolderPath: Path): Boolean {
        symbolicCopyOnlyRequiredExtensions(
            fromDirectory = projectPath.toRealPath(),
            toDirectory = projectTmpFolderPath,
            analysisExecutor.requiredFileExtensions,
        )
        val isSuccessful = analysisExecutor.execute(projectTmpFolderPath)
        projectTmpFolderPath.delete(true)
        return isSuccessful
    }
}

/**
 * Class for execution multiple analyzers for all projects in given directory.
 */
class MultipleAnalysisOrchestrator(
    analysisExecutors: List<AnalysisExecutor>,
    executorHelper: ExecutorHelper? = null
) : Orchestrator(executorHelper) {

    private val analyzersByExtensions = analysisExecutors.groupBy { it.requiredFileExtensions }

    override fun doAnalysis(projectPath: Path, projectTmpFolderPath: Path): Boolean {
        return analyzersByExtensions.map { (extensions, analyzers) ->
            symbolicCopyOnlyRequiredExtensions(
                fromDirectory = projectPath.toRealPath(),
                toDirectory = projectTmpFolderPath,
                extensions
            )

            val analyzersByOpener = analyzers.groupBy { it.repositoryOpener }
            val isSuccessful = analyzersByOpener.map { (opener, analyzers) ->
                MultipleAnalysisExecutor(
                    analyzers,
                    repositoryOpener = opener
                ).execute(projectTmpFolderPath)
            }.all { it }

            projectTmpFolderPath.delete(true)
            isSuccessful
        }.all { it }
    }
}
