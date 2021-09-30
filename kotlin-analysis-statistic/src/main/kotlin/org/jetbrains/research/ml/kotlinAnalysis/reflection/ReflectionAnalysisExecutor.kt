package org.jetbrains.research.ml.kotlinAnalysis.reflection

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import java.nio.file.Path

/**
 * Executor for reflections analysis which collects projects that use java reflection functions
 * and stores them to the file in [output directory][outputDir].
 */
class ReflectionAnalysisExecutor(
    outputDir: Path,
    reflectionFilename: String = "reflection_data.csv"
) : AnalysisExecutor() {

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
