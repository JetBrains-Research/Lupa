package org.jetbrains.research.ml.kotlinAnalysis.tagging

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import java.nio.file.Path

/**
 * Executor for tagging projects from dataset as android/other/undefined.
 * Can be extended for more types of projects labeling.
 */
class ProjectsTaggingExecutor(outputDir: Path, filename: String = "not_android_projects_data.csv") :
    AnalysisExecutor() {

    private val logger: Logger = Logger.getInstance(javaClass)

    companion object {

        /** Library name which indicates that the project is android. */
        const val ANDROID_DEPENDENCY_GROUP_NAME = "com.android.tools.build"
    }

    private val projectsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("full_name", "tag").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectsDataWriter)

    override fun analyse(project: Project) {
        projectsDataWriter.writer.println(
            listOf(
                project.name.replace('#', '/'),
                project.getTag().value
            ).joinToString(separator = ",")
        )
    }

    /**
     * If there is no gradle file in project, project tag is [ProjectTag.UNDEFINED];
     * If there is gradle file contains [ANDROID_DEPENDENCY_GROUP_NAME], project tag is [ProjectTag.ANDROID];
     * Other tags are not implemented now, so otherwise project tag is [ProjectTag.OTHER].
     */
    private fun Project.getTag(): ProjectTag {
        val gradleFile = GradleFileManager.extractRootGradleFileFromProject(this)
            ?: run {
                logger.info("Can not find gradle file in project ${this.name}")
                return ProjectTag.UNDEFINED
            }

        if (gradleFile.containsDependencyWithGroup(ANDROID_DEPENDENCY_GROUP_NAME)) {
            logger.info("Project ${this.name} is android project")
            return ProjectTag.ANDROID
        }

        return ProjectTag.OTHER
    }
}
