package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import java.io.File

@RunWith(Parameterized::class)
class StdlibInterfacesAnalyzerTest :
    ParametrizedBaseTest(getResourcesRootPath(::StdlibInterfacesAnalyzerTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::StdlibInterfacesAnalyzerTest, outExtension = Extension.TXT)
    }

    @Test
    fun testInternalDeclarationInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val p = myFixture.project
        val actualInternalDeclarationResults = inPsiFile.extractElementsOfType(KtClass::class.java)
            .mapNotNull { StdlibInterfacesAnalyzer.analyze(it) }
        val expectedInternalDeclarationResults = outFile!!.readLines().sorted()

//        Assert.assertTrue(expectedInternalDeclarationResults.size == actualInternalDeclarationResults.size)
//        expectedInternalDeclarationResults.zip(actualInternalDeclarationResults).forEach { (expected, actual) ->
//            Assert.assertEquals(expected, actual.toString())
//        }
    }
}
