package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.nio.file.Path

class ProjectSetupUtil {

    companion object {

        private val logger: Logger = Logger.getInstance(ProjectSetupUtil::class.java)

        private fun refreshProject(project: Project) {
            if (MavenProjectsManager.getInstance(project).isMavenizedProject) {
                logger.info("Refreshing Maven project ${project.name}")
                MavenProjectsManager.getInstance(project).scheduleImportAndResolve()
                MavenProjectsManager.getInstance(project).importProjects()
            } else {
                logger.info("Refreshing Gradle project ${project.name}")
                val specBuilder = ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
                ExternalSystemUtil.refreshProjects(specBuilder)
            }
        }

        fun setUpProject(projectPath: Path): Project? {
            // To open project we to create new project and link opening project to it
            // To load all modules we need to run refresh, but it do not help now
            return ProjectManagerEx.getInstanceEx()
                .openProject(
                    projectPath,
                    OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                )
                ?.also { refreshProject(it) }
        }
    }
}
