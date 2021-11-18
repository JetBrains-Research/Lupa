package org.jetbrains.research.ml.kotlinAnalysis.inlineFunction

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.*
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import java.nio.file.Path

class InlineFunctionAnalysisExecutor(
    outputDir: Path,
    superCallFromInlineFunctionsFilename: String = "super_call_from_inline_functions_data.csv",
    constructorCallFromInlineFunctionsFilename: String = "constructor_call_from_inline_functions_data.csv",
) : AnalysisExecutor() {
    private val superCallInlineFunctionsDataWriter = PrintWriterResourceManager(
        outputDir, superCallFromInlineFunctionsFilename,
        listOf("project_name", "count")
            .joinToString(separator = ",")
    )

    private val constructorCallInlineFunctionsDataWriter = PrintWriterResourceManager(
        outputDir, constructorCallFromInlineFunctionsFilename,
        listOf("project_name", "count")
            .joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(
        superCallInlineFunctionsDataWriter,
        constructorCallInlineFunctionsDataWriter
    )

    override fun analyse(project: Project) {
        val allSuperCallElements = project.extractKtElementsOfType(KtSuperExpression::class.java)
        val superCallCount =
            allSuperCallElements.map { SuperCallFromInlineFunctionAnalyzer.analyze(it) }.filter { it }.size
        superCallInlineFunctionsDataWriter.writer.println(
            listOf(project.name, superCallCount).joinToString(separator = ",")
        )

        val allCallExpressionElements = project.extractKtElementsOfType(KtCallExpression::class.java)
        val callExpressionCount =
            allCallExpressionElements.map { ConstructorFromInlineFunctionAnalyzer.analyze(it) }.filter { it }.size
        constructorCallInlineFunctionsDataWriter.writer.println(
            listOf(project.name, callExpressionCount).joinToString(separator = ",")
        )
    }
}

