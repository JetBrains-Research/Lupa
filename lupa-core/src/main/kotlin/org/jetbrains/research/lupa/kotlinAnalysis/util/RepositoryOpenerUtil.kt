package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.python.inspections.PythonPluginCommandLineInspectionProjectConfigurator
import org.jetbrains.research.pluginUtilities.openRepository.RepositoryOpener
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
            action: (Project) -> Boolean,
        ): Boolean {
            if (getKotlinJavaRepositoryOpener().openRepository(repositoryRoot.toFile(), action)) {
                logger.info("All projects from $repositoryRoot were opened successfully")
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
            action: (Project) -> Boolean,
        ): Boolean {
            if (getKotlinJavaRepositoryOpener().openSingleProject(projectPath, action)) {
                logger.info("All projects from $projectPath were opened successfully")
                return true
            }
            return false
        }

        // This function will work correctly ONLY IF venv will be created and moved
        // into the root folder of the project
        fun pythonRepositoryOpenerWithResolve(
            projectPath: Path,
            action: (Project) -> Boolean,
        ): Boolean {
            return RepositoryOpener(emptyList()).openProjectWithResolve(projectPath, action) {
                PythonPluginCommandLineInspectionProjectConfigurator()
            }
        }
    }
}
