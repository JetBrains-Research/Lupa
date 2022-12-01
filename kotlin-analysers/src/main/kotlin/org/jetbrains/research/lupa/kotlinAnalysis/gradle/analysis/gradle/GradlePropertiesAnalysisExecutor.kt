package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle

import com.intellij.openapi.project.Project
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

/**
 * Executor for gradle properties analysis which collects property name and value from gradle.properties files
 * to csv file with columns "project_name", "property_key", "property_value".
 */
class GradlePropertiesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "gradle_properties_data.csv",
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val gradlePropertiesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "property_key", "property_value").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradlePropertiesDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = setOf(FileExtension.PROPERTIES)

    override fun analyse(project: Project) {
        GradleFileManager.extractGradlePropertiesFilesFromProject(project).forEach { gradlePropertiesFile ->
            gradlePropertiesFile.extractGradleProperties().let { gradleProperties ->
                gradleProperties.forEach {
                    gradlePropertiesDataWriter.writer.println(
                        listOf(
                            project.name,
                            it.key,
                            it.value,
                        ).joinToString(separator = ","),
                    )
                }
            }
        }
    }
}
