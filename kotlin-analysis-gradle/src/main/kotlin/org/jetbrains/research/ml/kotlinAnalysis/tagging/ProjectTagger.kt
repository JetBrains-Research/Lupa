package org.jetbrains.research.ml.kotlinAnalysis.tagging

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.tagging.AndroidProjectTagger.ANDROID_DEPENDENCY_GROUP_NAME

/** Interface for project taggers, which can tag the project if they can define a tag. */
interface ProjectTagger {
    fun getProjectTag(project: Project): Set<ProjectTag>
}

/** Tags [ProjectTag.ANDROID] project if their root gradle file contains [ANDROID_DEPENDENCY_GROUP_NAME] dependency. */
object AndroidProjectTagger : ProjectTagger {

    /** Library name which indicates that the project is android. */
    private const val ANDROID_DEPENDENCY_GROUP_NAME = "com.android.tools.build"

    override fun getProjectTag(project: Project): Set<ProjectTag> {
        return GradleFileManager.extractRootBuildGradleFileFromProject(project)
            ?.takeIf { it.containsDependencyWithGroupId(ANDROID_DEPENDENCY_GROUP_NAME) }
            ?.let { setOf(ProjectTag.ANDROID) }
            ?: emptySet()
    }
}
