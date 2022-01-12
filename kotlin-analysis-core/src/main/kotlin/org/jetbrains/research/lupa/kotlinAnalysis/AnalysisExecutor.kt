package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.ml.kotlinAnalysis.util.DatabaseUtil
import org.jetbrains.research.ml.kotlinAnalysis.util.GitRepository
import java.nio.file.Path

/**
 * Abstract class for analysis executor which provides interface for execution analysis
 * for each project in given dataset.
 */
abstract class AnalysisExecutor {

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
            RepositoryOpenerUtil.Companion::standardRepositoryOpener
    ) {
        init()
        try {
            val db = DatabaseUtil()
            repositoryOpener(projectsDir, ::analyse) { repo -> db.updateRepoDate(repo.username, repo.repositoryName) }
        } finally {
            close()
        }
    }
}

/** Class for simultaneous execution of multiple analysis for each project in given dataset. */
class MultipleAnalysisExecutor(private val analysisExecutors: List<AnalysisExecutor>) : AnalysisExecutor() {

    override fun analyse(project: Project) {
        analysisExecutors.forEach { it.analyse(project) }
    }

    override val controlledResourceManagers = analysisExecutors.flatMap { it.controlledResourceManagers }.toSet()
}
