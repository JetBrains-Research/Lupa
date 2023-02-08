package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.GitRepository
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.lupa.kotlinAnalysis.util.getSubdirectories
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
    open val repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
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
    fun analyseWithResult(project: Project): Boolean {
        analyse(project)
        return isSuccessful(project)
    }

    /** Returns whether the analysis of given project was successful.
     * Default implementation always returns true.
     */
    open fun isSuccessful(project: Project): Boolean {
        return true
    }

    /** Runs before analysis execution process. Contains all controlled resources initialization.
     * @param relativePath relative path between output directory and file itself.
     */
    fun init(relativePath: Path? = null) {
        controlledResourceManagers.forEach { it.init(relativePath) }
    }

    /** Runs after analysis execution process. Closes all controlled resource. */
    fun close() {
        controlledResourceManagers.forEach { it.close() }
    }

    /** Executes analysis of given project.
     *  Also runs [executorHelper.postExecuteAction()] on each repository after processing it.
     *
     *  @return whether the analysis of project was successful.
     */
    fun execute(
        projectPath: Path,
        relativePathBuilder: (projectPath: Path) -> Path? = ::buildRelativePathWithDate
    ): Boolean {
        val isSuccessful: Boolean
        init(relativePathBuilder(projectPath))
        try {
            isSuccessful = repositoryOpener(projectPath, ::analyseWithResult)
            if (isSuccessful) {
                executorHelper?.postExecuteAction(GitRepository(projectPath))
            } else {
                return false
            }
        } finally {
            close()
        }
        return true
    }

    /** Executes analysis for all projects in [given directory][projectsDir].
     *
     *  @return whether the analysis of the whole dataset was successful.
     */
    fun executeAllProjects(
        projectsDir: Path,
        relativePathBuilder: (projectPath: Path) -> Path? = ::buildRelativePathWithDate
    ): Boolean {
        return getSubdirectories(projectsDir).mapIndexed { projectIndex, projectPath ->
            println("Start analysing $projectPath index=$projectIndex time=${System.currentTimeMillis()}")
            val isSuccessful = execute(projectPath, relativePathBuilder)
            println(
                "Finish analysing $projectPath index=$projectIndex time=${System.currentTimeMillis()}. " +
                        "Success: $isSuccessful"
            )
            isSuccessful
        }.all { it }
    }

    private fun buildRelativePathWithDate(projectPath: Path) = Paths.get(LocalDate.now().toString(), projectPath.name)
}

/** Class for simultaneous execution of multiple analysis for each project in given dataset
 * or analysis of project itself.
 *
 * @property analysisExecutors list of analysis executors to be run on the dataset.
 * @property executorHelper contains post-execution action to perform after project analysis.
 * @property repositoryOpener opener of projects.
 */
class MultipleAnalysisExecutor(
    private val analysisExecutors: List<AnalysisExecutor>,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean = RepositoryOpenerUtil.Companion::standardRepositoryOpener
) : AnalysisExecutor(executorHelper, repositoryOpener) {

    override fun analyse(project: Project) = analysisExecutors.forEach { it.analyse(project) }

    override fun isSuccessful(project: Project) = analysisExecutors.map { it.isSuccessful(project) }.all { it }

    override val controlledResourceManagers = analysisExecutors.flatMap { it.controlledResourceManagers }.toSet()
    override val requiredFileExtensions: Set<FileExtension> =
        analysisExecutors.flatMap { it.requiredFileExtensions }.toSet()
}

/** Classes that inherit from this interface implement action to perform after repository analysis. */
interface ExecutorHelper {
    fun postExecuteAction(repo: GitRepository) {}
}
