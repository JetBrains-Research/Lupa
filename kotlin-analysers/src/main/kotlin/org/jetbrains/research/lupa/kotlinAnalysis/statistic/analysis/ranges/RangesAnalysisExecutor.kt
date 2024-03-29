package org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.ranges

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.refactoring.getUsageContext
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpressionImpl
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.getRelativePathToKtElement
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.KOTLIN_EXTENSIONS
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path
import java.util.*

/**
 * Executor for ranges analysis which collects range and context types of all ranges usages in projects
 * and stores them to the file in [output directory][outputDir].
 * Also, it stores information about ranges usage with undefined context.
 */
class RangesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    rangesFilename: String = "ranges_data.csv",
    otherContextFilename: String = "other_context.csv",
) : AnalysisExecutor(executorHelper, repositoryOpener) {

    private val rangeAndContextPairs = getRangesAndContextPairs()

    private val rangesDataWriter = PrintWriterResourceManager(outputDir, rangesFilename, getRangesHeader())
    private val otherContextDataWriter =
        PrintWriterResourceManager(outputDir, otherContextFilename, getOtherContextHeader())
    override val controlledResourceManagers: Set<ResourceManager> = setOf(rangesDataWriter, otherContextDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = KOTLIN_EXTENSIONS

    override fun analyse(project: Project) {
        val binaryExpressionRanges = project.extractKtElementsOfType(KtBinaryExpression::class.java)
        val binaryExpressionResults =
            binaryExpressionRanges.associateWith { BinaryExpressionRangesPsiAnalyzer.analyze(it) }

        val callExpressionRanges = project.extractKtElementsOfType(KtCallExpression::class.java)
        val callExpressionResults = callExpressionRanges.associateWith { CallExpressionRangesPsiAnalyzer.analyze(it) }

        val elementToRangeAndContext = (callExpressionResults + binaryExpressionResults)
            .filter { it.value != RangeType.NOT_RANGE }
            .mapValues { (element, range) ->
                Pair(range, RangesContextAnalyzer.analyze(element))
            }

        saveProjectStats(project, elementToRangeAndContext)
        saveOtherContextStats(elementToRangeAndContext)
    }

    private fun saveProjectStats(
        project: Project,
        elementToRangeAndContext: Map<KtExpressionImpl, Pair<RangeType, ContextType>>,
    ) {
        val rangesAndContextStats = elementToRangeAndContext.values.groupingBy { it }.eachCount()

        val rangesStats = rangeAndContextPairs
            .map { type -> rangesAndContextStats.getOrDefault(type, 0) }
            .joinToString(separator = "\t")
        rangesDataWriter.writer.println("${project.name}\t$rangesStats")
    }

    /**
     * This method saves metadata about range usages with undefined context.
     *
     * @param elementToRangeAndContext mapping from PSI element (that corresponds to the range usage)
     * to it's range and context type
     * @param smallContextNParents parent number, which text will be saved as a small context of range usage
     * @param contextNParents parent number, which text will be saved as a full context of range usage
     * @param nParentsTypes number of parents, whose types will be saved
     */
    private fun saveOtherContextStats(
        elementToRangeAndContext: Map<KtExpressionImpl, Pair<RangeType, ContextType>>,
        smallContextNParents: Int = 3,
        contextNParents: Int = 6,
        nParentsTypes: Int = 10,
    ) {
        val elements = elementToRangeAndContext.filter { it.value.second == ContextType.OTHER }.keys
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
                ).joinToString(separator = ","),
            )
        }
    }

    private fun getRangesAndContextPairs(): List<Pair<RangeType, ContextType>> {
        return RangeType.valuesNotNull()
            .flatMap { range -> ContextType.values().map { context -> Pair(range, context) } }
    }

    private fun getRangesHeader(): String {
        val rangesHeader = rangeAndContextPairs
            .joinToString(separator = "\t") { "${it.first},${it.second}".lowercase(Locale.getDefault()) }
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
