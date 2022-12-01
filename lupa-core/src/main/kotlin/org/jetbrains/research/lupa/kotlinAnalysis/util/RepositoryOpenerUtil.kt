package org.jetbrains.research.lupa.kotlinAnalysis.util

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
         * Opens and reloads project in given directory by [path][repositoryRoot] and then
         * runs [action] on opened project.
         * This opening method allow to get project structure (all modules), but is more time consuming then
         * [standardRepositoryOpener].
         */
        fun openReloadKotlinJavaRepositoryOpener(
            repositoryRoot: Path,
            action: (Project) -> Boolean
        ): Boolean {
            if (getKotlinJavaRepositoryOpener().openRepository(
                    repositoryRoot.toFile()
                ) { project ->
                    action(project)
                }
            ) {
                println("All projects from $repositoryRoot were opened successfully")
                return true
            }
            return false
        }

        /**
         * Just opens project in given repository by [path][projectPath] and then
         * runs [action] on opened project.
         * This opening method do not provide right project modules structure, but is fast.
         */
        fun standardRepositoryOpener(
            projectPath: Path,
            action: (Project) -> Boolean
        ): Boolean {
            var isSuccessful = true
            try {
                ApplicationManager.getApplication().invokeAndWait {
                    ProjectManagerEx.getInstanceEx().openProject(
                        projectPath,
                        OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                    )?.let { project ->
                        try {
                            isSuccessful = action(project)
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
            } catch (ex: Exception) {
                logger.error(ex)
                isSuccessful = false
            }
            return isSuccessful
        }
    }
}
