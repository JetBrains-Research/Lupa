package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import java.nio.file.Path

class AndroidProjectsFilterExecutor(outputDir: Path, filename: String = "not_android_projects_data.csv") :
    AnalysisExecutor() {

    private val logger: Logger = Logger.getInstance(javaClass)

    companion object {
        const val ANDROID_DEPENDENCY_GROUP_NAME = "com.android.tools.build"
    }

    private val projectsDataWriter = PrintWriterResourceManager(outputDir, filename, "full_name")
    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectsDataWriter)

    override fun analyse(project: Project) {
        val gradleFile = GradleFileManager.extractRootBuildGradleFileFromProject(project)
            ?: run {
                logger.info("Can not find gradle file in project ${project.name}")
                return
            }

        if (gradleFile.containsDependencyWithGroup(ANDROID_DEPENDENCY_GROUP_NAME)) {
            logger.info("Project ${project.name} is android project")
        } else {
            projectsDataWriter.writer.println(project.name.replace('#', '/'))
        }
    }
}
