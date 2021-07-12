package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import java.nio.file.Path


class RangesAnalysisExecutor(outputDir: Path) : AnalysisExecutor() {

    private val rangesDataWriter = PrintWriterResourceManager(outputDir, "ranges_data.csv")
    override val controlledResourceManagers: Set<ResourceManager> = setOf(rangesDataWriter)

    override fun analyse(project: Project) {
        val binaryExpressionRanges =
            PsiProvider.extractElementsOfTypeFromProject(project, KtBinaryExpression::class.java)
        val binaryExpressionResults = binaryExpressionRanges.map { BinaryExpressionRangesPsiAnalyzer.analyze(it) }
            .filter { it != RangeType.NOT_RANGE }.groupingBy { it }.eachCount()
        binaryExpressionResults.forEach { (range, count) ->
            rangesDataWriter.writer.println("$range,$count")
        }

        println("------------------------")

        val callExpressionRanges =
            PsiProvider.extractElementsOfTypeFromProject(project, KtCallExpression::class.java)
        val callExpressionResults = callExpressionRanges.map { CallExpressionRangesPsiAnalyzer.analyze(it) }
    }
}
