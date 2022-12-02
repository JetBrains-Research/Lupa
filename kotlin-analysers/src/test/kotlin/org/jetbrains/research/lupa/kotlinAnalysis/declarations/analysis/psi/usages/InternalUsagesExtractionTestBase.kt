package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesAnalysisResult
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import java.io.File

interface InternalUsagesExtractionTestBase {
    fun testInternalUsageInFile(
        inFile: File,
        outFile: File,
        fixture: CodeInsightTestFixture,
        clazz: Class<out KtElement>,
        analyze: (KtElement) -> List<InternalUsagesAnalysisResult>?,
    ) {
        val inPsiFile = getPsiFile(inFile, fixture)
        val actualInternalDeclarationResults = inPsiFile.extractElementsOfType(clazz)
            .mapNotNull { analyze(it) }
            .flatten()
            .sortedWith(
                compareBy(
                    InternalUsagesAnalysisResult::declarationFqName,
                    InternalUsagesAnalysisResult::usageFqName,
                ),
            )
        val expectedInternalDeclarationResults = outFile.readLines().sorted()

        Assert.assertTrue(expectedInternalDeclarationResults.size == actualInternalDeclarationResults.size)
        expectedInternalDeclarationResults.zip(actualInternalDeclarationResults).forEach { (expected, actual) ->
            Assert.assertEquals(expected, actual.toString())
        }
    }
}
