package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.util.io.delete
import org.jetbrains.research.lupa.kotlinAnalysis.util.*
import java.nio.file.Path
import java.nio.file.Paths

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

        val groupedByAnalyzers = analysisExecutors.groupBy { it.requiredFileExtensions }

        getSubdirectories(projectsDir).forEachIndexed { projectIndex, projectPath ->
            println("Start analyzing project ${projectPath.fileName} (index=$projectIndex)")
            val projectTmpFolderPath = tempFolderPath.resolve(projectPath.fileName)
            val projectSuccessfullyAnalyzed = groupedByAnalyzers.map { (extensions, analyzers) ->
                symbolicCopyOnlyRequiredExtensions(
                    fromDirectory = projectPath.toRealPath(),
                    toDirectory = projectTmpFolderPath,
                    extensions
                )
                val isSuccessful = MultipleAnalysisExecutor(
                    analyzers,
                    repositoryOpener = opener(extensions)
                ).execute(projectTmpFolderPath)

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

    private fun opener(extensions: Set<FileExtension>): (Path, (Project) -> Boolean) -> Boolean {
        return if (extensions.isEmpty()) {
            RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener
        } else {
            RepositoryOpenerUtil.Companion::standardRepositoryOpener
        }
    }
}
