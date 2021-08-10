package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleFileManager
import java.nio.file.Path

/**
 * Executor for gradle properties analysis which collects property name and value from gradle.properties file
 * to csv file with columns "project_name", "property_key", "property_value".
 */
class GradlePropertiesAnalysisExecutor(outputDir: Path, filename: String = "gradle_dependencies_data.csv") :
    AnalysisExecutor() {

    private val logger: Logger = Logger.getInstance(javaClass)

    private val gradleDependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        listOf("project_name", "property_key", "property_value")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(gradleDependenciesDataWriter)

    override fun analyse(project: Project) {
        GradleFileManager
            .extractGradlePropertiesFileFromProject(project)
            ?.extractGradleProperties()
            ?.let { gradleProperties ->
                gradleDependenciesDataWriter.writer.write(
                    gradleProperties.joinToString(separator = System.getProperty("line.separator")) {
                        listOf(
                            project.name,
                            it.key,
                            it.value
                        ).joinToString(separator = ",")
                    }
                )
            }
            ?: logger.info("Can not get gradle.properties file from project: ${project.name}")
    }
}
