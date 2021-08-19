package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.refactoring.getUsageContext
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpressionImpl
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.getRelativePathToKtElement
import java.nio.file.Path

/**
 * Wrapper for data about range -- it's type, context and pair of identifiers used as left and right border.
 */
data class RangeData(val rangeType: RangeType, val rangeContext: ContextType, val identifiers: Pair<String, String>)

/**
 * Executor for ranges analysis which collects range and context types of all ranges usages in projects
 * and stores them to the file in output directory.
 * Also, it stores information about ranges usage with undefined context.
 */
class RangesAnalysisExecutor(
    outputDir: Path,
    rangesStatsFilename: String = "ranges_stats.csv",
    rangesDataFilename: String = "ranges_data.csv",
    otherContextFilename: String = "other_context.csv",
) : AnalysisExecutor() {

    private val rangeAndContextPairs = getRangesAndContextPairs()

    private val rangesStatsWriter = PrintWriterResourceManager(outputDir, rangesStatsFilename, getRangesHeader())
    private val otherContextDataWriter =
        PrintWriterResourceManager(outputDir, otherContextFilename, getOtherContextHeader())
    private val rangesDataWriter = PrintWriterResourceManager(
        outputDir, rangesDataFilename,
        listOf("project", "range_type", "context", "left_border", "right_border").joinToString(separator = "\t")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(
        rangesStatsWriter,
        otherContextDataWriter,
        rangesDataWriter
    )

    override fun analyse(project: Project) {
        val binaryExpressionRanges = project.extractElementsOfType(KtBinaryExpression::class.java)
        val binaryExpressionResults =
            binaryExpressionRanges.associateWith { BinaryExpressionRangesPsiAnalyzer.analyze(it) }

        val callExpressionRanges = project.extractElementsOfType(KtCallExpression::class.java)
        val callExpressionResults = callExpressionRanges.associateWith { CallExpressionRangesPsiAnalyzer.analyze(it) }

        val elementToRangeData = (callExpressionResults + binaryExpressionResults)
            .filter { it.value != RangeType.NOT_RANGE }
            .mapValues { (element, range) ->
                RangeData(
                    range,
                    RangesContextAnalyzer.analyze(element),
                    RangesIdentifierLengthAnalyzer.analyze(element)!!
                )
            }

        saveProjectStats(project, elementToRangeData)
        saveOtherContextStats(elementToRangeData)
        saveRangeData(project, elementToRangeData)
    }

    private fun saveRangeData(
        project: Project,
        elementToRangeData: Map<KtExpressionImpl, RangeData>
    ) {
        elementToRangeData.forEach { (_, data) ->
            rangesDataWriter.writer.println(
                listOf(
                    project.name,
                    data.rangeType.name.toLowerCase(),
                    data.rangeContext.name.toLowerCase(),
                    escapeString(data.identifiers.first),
                    escapeString(data.identifiers.second)
                ).joinToString(separator = "\t")
            )
        }
    }

    private fun saveProjectStats(
        project: Project,
        elementToRangeData: Map<KtExpressionImpl, RangeData>
    ) {
        val rangesAndContextStats =
            elementToRangeData.values.map { rangeData -> Pair(rangeData.rangeType, rangeData.rangeContext) }
                .groupingBy { it }
                .eachCount()

        val rangesStats = rangeAndContextPairs
            .map { type -> rangesAndContextStats.getOrDefault(type, 0) }
            .joinToString(separator = "\t")
        rangesStatsWriter.writer.println("${project.name}\t$rangesStats")
    }

    /**
     * This method saves metadata about range usages with undefined context.
     *
     * @param elementToRangeData mapping from PSI element (that corresponds to the range usage)
     * to it's range and context type
     * @param smallContextNParents parent number, which text will be saved as a small context of range usage
     * @param contextNParents parent number, which text will be saved as a full context of range usage
     * @param nParentsTypes number of parents, whose types will be saved
     */
    private fun saveOtherContextStats(
        elementToRangeData: Map<KtExpressionImpl, RangeData>,
        smallContextNParents: Int = 3, contextNParents: Int = 6, nParentsTypes: Int = 10
    ) {
        val elements = elementToRangeData.filter { it.value.rangeContext == ContextType.OTHER }.keys
        elements.forEach { psiElement ->
            val relativeFilePath = psiElement.getRelativePathToKtElement()
            val smallContext = escapeString(psiElement.parents.take(smallContextNParents).last().text)
            val contextText = escapeString(psiElement.parents.take(contextNParents).last().text)
            val tenParentsClasses = psiElement.parents.take(nParentsTypes).map { it::class.java.simpleName }.toList()
                .joinToString(prefix = "\"[", postfix = "]\"", separator = ",")
            val usageContextText = escapeString(psiElement.getUsageContext().text)

            otherContextDataWriter.writer.println(
                listOf(
                    relativeFilePath,
                    smallContext,
                    contextText,
                    tenParentsClasses,
                    usageContextText,
                ).joinToString(separator = ",")
            )
        }
    }

    private fun getRangesAndContextPairs(): List<Pair<RangeType, ContextType>> {
        return RangeType.valuesNotNull()
            .flatMap { range -> ContextType.values().map { context -> Pair(range, context) } }
    }

    private fun getRangesHeader(): String {
        val rangesHeader = rangeAndContextPairs
            .joinToString(separator = "\t") { "${it.first},${it.second}".toLowerCase() }
        return "project\t$rangesHeader"
    }

    private fun getOtherContextHeader(): String {
        return listOf("path", "small_context", "context", "parents_classes", "usage_context")
            .joinToString(separator = ",")
    }

    private fun escapeString(s: String): String {
        return "\"" + s.replace("\"", "\"\"") + "\""
    }
}
