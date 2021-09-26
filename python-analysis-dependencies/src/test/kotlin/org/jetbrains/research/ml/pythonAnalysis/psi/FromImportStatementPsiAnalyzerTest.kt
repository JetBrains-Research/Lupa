package org.jetbrains.research.ml.pythonAnalysis.psi

import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.pythonAnalysis.FromImportStatementPsiAnalyzer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class FromImportStatementPsiAnalyzerTest : ParametrizedBaseTest(
    getResourcesRootPath(
        ::FromImportStatementPsiAnalyzerTest,
        resourcesRootName = "fromImportStatementsData"
    )
) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() =
            getInAndOutArray(
                ::FromImportStatementPsiAnalyzerTest,
                resourcesRootName = "fromImportStatementsData",
                inExtension = Extension.PY,
                outExtension = Extension.TXT,
            )
    }

    @Test
    fun testImportStatementsFqNamesInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)

        val actualFromImportStatementFqNames = inPsiFile.extractElementsOfType(PyFromImportStatement::class.java)
            .map { FromImportStatementPsiAnalyzer.analyze(it) }.flatten().sorted()

        val expectedFromImportStatementFqNames = outFile!!.readLines().sorted()

        Assert.assertEquals(expectedFromImportStatementFqNames, actualFromImportStatementFqNames)
    }
}
