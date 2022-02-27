package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.util.io.delete
import org.jetbrains.research.lupa.kotlinAnalysis.util.*
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Classes that inherit from this interface filter the project files by the extension needed to perform the analysis.
 */
interface Orchestrator {
    fun execute(projectsDir: Path, outputDir: Path)
}

/**
 * Class for execution analyzer for all projects in given directory.
 */
class AnalysisOrchestrator(
    private val analysisExecutor: AnalysisExecutor,
    private val executorHelper: ExecutorHelper? = null
) : Orchestrator {

    /**
     * Executes analysis of all projects in dataset using [analyzer][analysisExecutor].
     * Also runs [executorHelper.postExecuteAction()] on each repository after processing it.
     */
    override fun execute(projectsDir: Path, outputDir: Path) {
        val tempFolderPath = Paths.get(outputDir.toString(), "tmp")
        tempFolderPath.delete(recursively = true)

        getSubdirectories(projectsDir).forEachIndexed { projectIndex, projectPath ->
            println("Start analyzing project ${projectPath.fileName} (index=$projectIndex)")

            val projectTmpFolderPath = tempFolderPath.resolve(projectPath.fileName)

            symbolicCopyOnlyRequiredExtensions(
                fromDirectory = projectPath.toRealPath(),
                toDirectory = projectTmpFolderPath,
                analysisExecutor.requiredFileExtensions,
            )

            val isSuccessful = analysisExecutor.execute(projectTmpFolderPath)

            projectTmpFolderPath.delete(true)

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
 * Class for execution multiple analyzers for all projects in given directory.
 */
class MultipleAnalysisOrchestrator(
    private val analysisExecutors: List<AnalysisExecutor>,
    private val executorHelper: ExecutorHelper? = null
) {

    /**
     * Executes analysis of all projects in dataset
     * using all analyzers from [list of analysis executors][analysisExecutors].
     * Also runs [executorHelper.postExecuteAction()] on each repository after processing it.
     */
    fun execute(projectsDir: Path, outputDir: Path) {
        val tempFolderPath = Paths.get(outputDir.toString(), "tmp")
        tempFolderPath.delete(recursively = true)

        val analyzersByExtensions = analysisExecutors.groupBy { it.requiredFileExtensions }

        getSubdirectories(projectsDir).forEachIndexed { projectIndex, projectPath ->
            println("Start analyzing project ${projectPath.fileName} (index=$projectIndex)")
            val projectTmpFolderPath = tempFolderPath.resolve(projectPath.fileName)
            val projectSuccessfullyAnalyzed = analyzersByExtensions.map { (extensions, analyzers) ->
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

            println("Project ${projectPath.fileName} (index=$projectIndex) " +
                    "was analyzed successfully: $projectSuccessfullyAnalyzed")
            if (projectSuccessfullyAnalyzed) {
                executorHelper?.postExecuteAction(GitRepository(projectPath))
            }
        }
        tempFolderPath.delete()
    }
}
