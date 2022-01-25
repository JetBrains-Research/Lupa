package org.jetbrains.research.lupa.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.util.io.delete
import org.jetbrains.research.lupa.kotlinAnalysis.util.*
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.io.path.name

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
    fun init(relativePath: String) {
        controlledResourceManagers.forEach { it.init(relativePath) }
    }

    /** Runs after analysis execution process. Closes all controlled resource. */
    fun close() {
        controlledResourceManagers.forEach { it.close() }
    }

    /** Executes analysis for all projects in [given directory][projectsDir]. */
    fun execute(
        projectsDir: Path,
        repositoryOpener: (Path, (Project) -> Unit, ((GitRepository) -> Unit)?) -> Unit =
            RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    ) {
        getSubdirectories(projectsDir).forEachIndexed { projectIndex, projectPath ->
            println("start analysing $projectPath")
            init(Paths.get(LocalDate.now().toString(), projectPath.name).toString())
            try {
                repositoryOpener(projectPath, ::analyseWithResult) { repo -> executorHelper?.postExecuteAction(repo) }
            } finally {
                close()
            }
            println("finish analysing $projectPath")
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

    override fun analyseWithResult(project: Project): Boolean {
        return analysisExecutors.map { it.analyseWithResult(project) }.all { it }
    }

    override val controlledResourceManagers = analysisExecutors.flatMap { it.controlledResourceManagers }.toSet()
    override val requiredFileExtensions: Set<FileExtension> =
        analysisExecutors.flatMap { it.requiredFileExtensions }.toSet()
}


class MultipleAnalysisOrchestrator(
    private val analysisExecutors: List<AnalysisExecutor>,
    val executorHelper: ExecutorHelper? = null
) {

    fun execute(projectsDir: Path, outputDir: Path) {
        val tempFolderPath = Paths.get(outputDir.toString(), "tmp")

        analysisExecutors.groupBy { it.requiredFileExtensions }.forEach { (extensions, analyzers) ->
            symbolicCopyOnlyRequiredExtensions(fromDirectory = projectsDir, toDirectory = tempFolderPath, extensions)
            MultipleAnalysisExecutor(analyzers, executorHelper).execute(tempFolderPath)
            tempFolderPath.delete(true)
        }
    }
}


/** Classes that inherit from this interface implement action to perform after repository analysis. */
interface ExecutorHelper {
    fun postExecuteAction(repo: GitRepository) {}
}
