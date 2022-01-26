package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.util.*
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.io.path.name

/**
 * Abstract class for analysis executor which provides interface for execution analysis
 * for each project in given dataset or analysis of project itself.
 * @property executorHelper contains post-execution action to perform after project analysis.
 * @property repositoryOpener opener of projects.
 */
abstract class AnalysisExecutor(
    protected open val executorHelper: ExecutorHelper? = null,
    protected open val repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener
) {

    /**
     * Set of resources which are under control of executor. Executor[AnalysisExecutor] runs their initialization
     * before analysis and close them after it ends.
     */
    abstract val controlledResourceManagers: Set<ResourceManager>

    /**
     * Set of extensions that are required for analysis execution. Empty set is used for the whole project analysis.
     */
    open val requiredFileExtensions: Set<FileExtension> = emptySet()

    /** Executes the analysis of the given [project][Project]. */
    abstract fun analyse(project: Project)

    /** Executes the analysis of the given [project][Project] and returns whether the analysis was successful. */
    open fun analyseWithResult(project: Project): Boolean {
        analyse(project)
        return true
    }

    /** Runs before analysis execution process. Contains all controlled resources initialization. */
    fun init(relativePath: String? = null) {
        controlledResourceManagers.forEach { it.init(relativePath) }
    }

    /** Runs after analysis execution process. Closes all controlled resource. */
    fun close() {
        controlledResourceManagers.forEach { it.close() }
    }

    /** Executes analysis of given project.
     *  Also runs [executorHelper.postExecuteAction()] on each repository after processing it.
     */
    fun execute(projectPath: Path): Boolean {
        val isSuccessful: Boolean
        init(Paths.get(LocalDate.now().toString(), projectPath.name).toString())
        try {
            isSuccessful = repositoryOpener(projectPath, ::analyseWithResult)
            if (isSuccessful) {
                executorHelper?.postExecuteAction(GitRepository(projectPath))
            }
        } finally {
            close()
        }
        return isSuccessful
    }

    /** Executes analysis for all projects in [given directory][projectsDir]. */
    fun executeAllProjects(projectsDir: Path): Boolean {
        return getSubdirectories(projectsDir).mapIndexed { projectIndex, projectPath ->
            println("Start analysing $projectPath index=$projectIndex time=${System.currentTimeMillis()}")
            val isSuccessful = execute(projectPath)
            println("Finish analysing $projectPath index=$projectIndex time=${System.currentTimeMillis()}. " +
                        "Success: $isSuccessful")
            isSuccessful
        }.all { it }
    }
}

/** Class for simultaneous execution of multiple analysis for each project in given dataset
 * or analysis of project itself. */
class MultipleAnalysisExecutor(
    private val analysisExecutors: List<AnalysisExecutor>,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean = RepositoryOpenerUtil.Companion::standardRepositoryOpener
) : AnalysisExecutor(executorHelper, repositoryOpener) {

    override fun analyse(project: Project) {
        analysisExecutors.forEach { it.analyse(project) }
    }

    override fun analyseWithResult(project: Project): Boolean {
        return analysisExecutors.map { it.analyseWithResult(project) }.all { it }
    }

    override val controlledResourceManagers = analysisExecutors.flatMap { it.controlledResourceManagers }.toSet()
    override val requiredFileExtensions: Set<FileExtension> =
        analysisExecutors.flatMap { it.requiredFileExtensions }.toSet()
}

/** Classes that inherit from this interface implement action to perform after repository analysis. */
interface ExecutorHelper {
    fun postExecuteAction(repo: GitRepository) {}
}
