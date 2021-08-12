package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import java.nio.file.Path

/** Class which contains several methods for opening project in given repository. */
class RepositoryOpenerUtil {
    companion object {

        private val logger: Logger = Logger.getInstance(RepositoryOpenerUtil::class.java)

        /**
         * Opens and reloads each project in given repository by [path][repositoryPath] and then
         * runs [action] on opened project.
         * This opening method allow to get project structure (all modules), but is more time consuming then
         * [standardRepositoryOpener].
         */
        fun openReloadRepositoryOpener(repositoryPath: Path, action: (Project) -> Unit) {
            var projectIndex = 0
            if (getKotlinJavaRepositoryOpener().openRepository(
                    repositoryPath.toFile()
                ) { project ->
                    runAction(project, projectIndex, action)
                    projectIndex += 1
                }
            ) {
                println("Not all projects from $repositoryPath was opened successfully")
            }
        }

        /**
         * Just opens each project in given repository by [path][repositoryPath] and then
         * runs [action] on opened project.
         * This opening method do not provide right project modules structure, but is fast.
         */
        fun standardRepositoryOpener(path: Path, action: (Project) -> Unit) {
            getSubdirectories(path).forEachIndexed { projectIndex, projectPath ->
                ApplicationManager.getApplication().invokeAndWait {
                    ProjectManagerEx.getInstanceEx().openProject(
                        projectPath,
                        OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                    )?.let { project ->
                        try {
                            runAction(project, projectIndex, action)
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
        }

        private fun runAction(project: Project, projectIndex: Int, action: (Project) -> Unit) {
            println("Start action on project ${project.name} index=$projectIndex time=${System.currentTimeMillis()}")
            action(project)
            println("Finish action on project ${project.name} index=$projectIndex time=${System.currentTimeMillis()}")
        }
    }
}
