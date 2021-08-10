package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import java.nio.file.Path

class RepositoryOpenerUtil {
    companion object {

        private val logger: Logger = Logger.getInstance(RepositoryOpenerUtil::class.java)

        fun openReloadRepositoryOpener(path: Path, action: (Project) -> Unit) {
            var projectIndex = 0
            if (getKotlinJavaRepositoryOpener().openRepository(
                    path.toFile()
                ) { project ->
                    println("Opening project ${project.name} index=$projectIndex time=${System.currentTimeMillis()}")
                    action(project)
                    projectIndex += 1
                }
            ) {
                println("Not all projects from $path was opened successfully")
            }
        }

        fun standardRepositoryOpener(path: Path, action: (Project) -> Unit) {
            getSubdirectories(path).forEachIndexed { index, projectPath ->
                ApplicationManager.getApplication().invokeAndWait {
                    println("Opening project $projectPath index=$index time=${System.currentTimeMillis()}")
                    ProjectManagerEx.getInstanceEx().openProject(
                        path,
                        OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                    )?.let { project ->
                        try {
                            action(project)
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
    }
}
