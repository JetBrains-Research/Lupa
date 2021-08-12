package org.jetbrains.research.ml.kotlinAnalysis.gradle

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import java.nio.file.Path

/**
 * Executor for gradle properties analysis which collects property name and value from gradle.properties files
 * to csv file with columns "project_name", "property_key", "property_value".
 */
class GradlePropertiesAnalysisExecutor(outputDir: Path, filename: String = "gradle_properties_data.csv") :
    AnalysisExecutor() {

    private val gradlePropertiesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "property_key", "property_value").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradlePropertiesDataWriter)

    override fun analyse(project: Project) {
        GradleFileManager
            .extractGradlePropertiesFilesFromProject(project).forEach { gradlePropertiesFile ->
                gradlePropertiesFile.extractGradleProperties().let { gradleProperties ->
                    gradleProperties.forEach {
                        gradlePropertiesDataWriter.writer.println(
                            listOf(
                                project.name,
                                it.key,
                                it.value
                            ).joinToString(separator = ",")
                        )
                    }
                }
            }
    }
}
