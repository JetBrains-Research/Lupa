package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.util.GitRepository
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

/**
 * Abstract class for analysis executor which provides interface for execution analysis
 * for each project in given dataset.
 * @property executorHelper contains post-execution action to perform after project analysis
 */
abstract class AnalysisExecutor(protected open val executorHelper: ExecutorHelper? = null) {

    /**
     * Set of resources which are under control of executor. Executor[AnalysisExecutor] runs their initialization
     * before analysis and close them after it ends.
     */
    abstract val controlledResourceManagers: Set<ResourceManager>

    /** Executes the analysis of the given [project][Project]. */
    abstract fun analyse(project: Project)

    /** Runs before analysis execution process. Contains all controlled resources initialization. */
    private fun init() {
        controlledResourceManagers.forEach { it.init() }
    }

    /** Runs after analysis execution process. Closes all controlled resource. */
    private fun close() {
        controlledResourceManagers.forEach { it.close() }
    }

    /** Executes analysis for all projects in [given directory][projectsDir]. */
    fun execute(
        projectsDir: Path,
        repositoryOpener: (Path, (Project) -> Unit, ((GitRepository) -> Unit)?) -> Unit =
            RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    ) {
        init()
        try {
            repositoryOpener(projectsDir, ::analyse) { repo -> executorHelper?.postExecuteAction(repo) }
        } finally {
            close()
        }
    }
}

/** Class for simultaneous execution of multiple analysis for each project in given dataset. */
class MultipleAnalysisExecutor(
    private val analysisExecutors: List<AnalysisExecutor>,
    executorHelper: ExecutorHelper? = null,
) : AnalysisExecutor(executorHelper) {

    override fun analyse(project: Project) {
        analysisExecutors.forEach { it.analyse(project) }
    }

    override val controlledResourceManagers = analysisExecutors.flatMap { it.controlledResourceManagers }.toSet()
}

/** Classes that inherit from this interface implement action to perform after repository analysis. */
interface ExecutorHelper {
    fun postExecuteAction(repo: GitRepository) {}
}
