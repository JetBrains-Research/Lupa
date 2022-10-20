package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages

import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesAnalysisResult
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesPsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class InternalUsagesExtractionTest : ParametrizedBaseTest(getResourcesRootPath(::InternalUsagesExtractionTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::InternalUsagesExtractionTest,
            outExtension = Extension.TXT
        ).filter { "in_3" in it.first().name }
    }

    @Test
    fun testInternalUsageInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val actualInternalDeclarationResults = inPsiFile.extractElementsOfType(KtNamedDeclaration::class.java)
            .mapNotNull { InternalUsagesPsiAnalyzer.analyze(it) }
            .flatten()
            .sortedWith(
                compareBy(
                    InternalUsagesAnalysisResult::declarationFqName,
                    InternalUsagesAnalysisResult::usageFqName
                )
            )
        val expectedInternalDeclarationResults = outFile!!.readLines().sorted()

        Assert.assertTrue(expectedInternalDeclarationResults.size == actualInternalDeclarationResults.size)
        expectedInternalDeclarationResults.zip(actualInternalDeclarationResults).forEach { (expected, actual) ->
            Assert.assertEquals(expected, actual.toString())
        }
    }
}
