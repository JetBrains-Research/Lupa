package org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.reflection

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.Configurable
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import java.nio.file.Path

/**
 * Executor for reflections analysis which collects projects that use java reflection functions
 * and stores them to the file in [output directory][outputDir].
 */
class ReflectionAnalysisExecutor(
    outputDir: Path,
    configData: Configurable? = null,
    reflectionFilename: String = "reflection_data.csv"
) : AnalysisExecutor(configData) {

    private val reflectionDataWriter = PrintWriterResourceManager(outputDir, reflectionFilename, "project")
    override val controlledResourceManagers: Set<ResourceManager> = setOf(reflectionDataWriter)

    override fun analyse(project: Project) {
        val binaryExpressionReflections = project.extractKtElementsOfType(KtNameReferenceExpression::class.java)
        val binaryExpressionResults =
            binaryExpressionReflections.associateWith { ReferenceExpressionReflectionPsiAnalyzer.analyze(it) }
                .filter { it.value != JavaReflectionFunction.NOT_REFLECTION }

        if (binaryExpressionResults.isNotEmpty()) {
            reflectionDataWriter.writer.println(project.name)
        }
    }
}
