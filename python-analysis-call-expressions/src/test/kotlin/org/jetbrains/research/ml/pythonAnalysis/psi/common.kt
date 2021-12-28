package org.jetbrains.research.ml.pythonAnalysis.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzer
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzerContext
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import java.io.File

fun <P : PyCallExpression> testCallExpressionFqNamesInFile(
    myFixture: CodeInsightTestFixture,
    inFile: File,
    outFile: File,
    analyzer: CallExpressionAnalyzer<P>,
    expressionPredicate: (PyCallExpression) -> Boolean,
) {
    val typeEvalContext = TypeEvalContext.deepCodeInsight(myFixture.project)
    val pyResolveContext = PyResolveContext.defaultContext(typeEvalContext)
    val fqNamesProvider = PyQualifiedNameProvider()

    val analyzerContext = CallExpressionAnalyzerContext(pyResolveContext, fqNamesProvider)

    val inPsiFile = getPsiFile(inFile, myFixture)

    val callExpressions = inPsiFile.extractElementsOfType(PyCallExpression::class.java).filter(expressionPredicate)

    val actualCallExpressionFqNames = callExpressions.mapNotNull { analyzer.analyze(it, analyzerContext) }.sorted()

    val expectedCallExpressionFqNames = outFile.readLines().sorted()

    BasePlatformTestCase.assertEquals(actualCallExpressionFqNames, expectedCallExpressionFqNames)
}
