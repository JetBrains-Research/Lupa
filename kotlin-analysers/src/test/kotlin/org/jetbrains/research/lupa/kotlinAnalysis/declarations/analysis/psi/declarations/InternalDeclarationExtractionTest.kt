package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.declarations

import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.declarations.InternalDeclarationPsiAnalyzer
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
class InternalDeclarationExtractionTest :
    ParametrizedBaseTest(getResourcesRootPath(::InternalDeclarationExtractionTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::InternalDeclarationExtractionTest, outExtension = Extension.TXT)
    }

    @Test
    fun testInternalDeclarationInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val actualInternalDeclarationResults = inPsiFile.extractElementsOfType(KtNamedDeclaration::class.java)
            .mapNotNull { InternalDeclarationPsiAnalyzer.analyze(it) }.sortedBy { it.fqName }
        val expectedInternalDeclarationResults = outFile!!.readLines().sorted()

        Assert.assertTrue(expectedInternalDeclarationResults.size == actualInternalDeclarationResults.size)
        expectedInternalDeclarationResults.zip(actualInternalDeclarationResults).forEach { (expected, actual) ->
            Assert.assertEquals(expected, actual.toString())
        }
    }
}
