package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import java.nio.file.Path


class RangesAnalysisExecutor(outputDir: Path) : AnalysisExecutor() {

    private val rangesDataWriter = PrintWriterResourceManager(outputDir, "ranges_data.csv", getHeader())
    override val controlledResourceManagers: Set<ResourceManager> = setOf(rangesDataWriter)

    override fun analyse(project: Project) {
        val binaryExpressionRanges =
            PsiProvider.extractElementsOfTypeFromProject(project, KtBinaryExpression::class.java)
        val binaryExpressionResults = binaryExpressionRanges.map { BinaryExpressionRangesPsiAnalyzer.analyze(it) }

        val callExpressionRanges =
            PsiProvider.extractElementsOfTypeFromProject(project, KtCallExpression::class.java)
        val callExpressionResults = callExpressionRanges.map { CallExpressionRangesPsiAnalyzer.analyze(it) }

        val rangeTypeToCount = (binaryExpressionResults + callExpressionResults)
            .filter { it != RangeType.NOT_RANGE }
            .groupingBy { it }.eachCount()

        val rangesStats = RangeType.valuesNotNull()
            .map { type -> rangeTypeToCount.getOrDefault(type, 0) }
            .joinToString(separator = "\t")
        rangesDataWriter.writer.println("${project.name}\t$rangesStats")
    }

    private fun getHeader(): String {
        val rangesHeader = RangeType.valuesNotNull()
            .joinToString(separator = "\t") { it.toString().toLowerCase() }
        return "project\t$rangesHeader"
    }
}
