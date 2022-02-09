package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.tagging

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

/**
 * Executor for tagging projects from dataset as android/other/undefined.
 * Can be extended for more types of projects labeling.
 */
class ProjectsTaggingExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "project_tags_data.csv"
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val projectsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "tag").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(projectsDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = setOf(FileExtension.GRADLE, FileExtension.KTS)

    private val taggers: List<ProjectTagger> = listOf(AndroidProjectTagger)

    override fun analyse(project: Project) {
        val projectTags = taggers.flatMapTo(mutableSetOf()) { it.getProjectTag(project) }
            .ifEmpty { setOf(ProjectTag.OTHER) }
            .map { it.value }

        projectsDataWriter.writer.println(
            listOf(
                project.name,
                projectTags.joinToString(separator = ",")
            ).joinToString(separator = ",")
        )
    }
}
