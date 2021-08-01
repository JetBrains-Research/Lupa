package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.ml.kotlinAnalysis.util.getSubdirectories
import java.nio.file.Path

/**
 * Abstract class for analysis executor which provides interface for execution analysis
 * for each project in given dataset.
 */
abstract class AnalysisExecutor {

    private val logger: Logger = Logger.getInstance(javaClass)

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
        setupProject: (Path) -> Project? = { projectPath ->
            ProjectManagerEx.getInstanceEx()
                .openProject(
                    projectPath,
                    OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                )
        }
    ) {
        init()
        try {
            getSubdirectories(projectsDir).forEachIndexed { index, projectPath ->
                ApplicationManager.getApplication().invokeAndWait {
                    println("Opening project $projectPath (index $index)")
                    setupProject(projectPath)?.let { project ->
                        try {
                            analyse(project)
                        } catch (ex: Exception) {
                            logger.error(ex)
                        } finally {
                            ApplicationManager.getApplication().invokeAndWait {
                                val closeStatus = ProjectManagerEx.getInstanceEx().forceCloseProject(project)
                                logger.info("Project ${project.name} is closed = $closeStatus")
                            }
                        }
                    }
                }
            }
        } finally {
            close()
        }
    }
}
