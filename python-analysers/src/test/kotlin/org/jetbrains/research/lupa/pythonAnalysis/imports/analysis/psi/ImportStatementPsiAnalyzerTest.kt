package org.jetbrains.research.lupa.pythonAnalysis.imports.analysis.psi

import com.jetbrains.python.psi.PyImportStatement
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.lupa.pythonAnalysis.imports.analysis.ImportStatementPsiAnalyzer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class ImportStatementPsiAnalyzerTest : ParametrizedBaseTest(
    getResourcesRootPath(
        ::ImportStatementPsiAnalyzerTest,
        resourcesRootName = "importStatementsData",
    ),
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
                ::ImportStatementPsiAnalyzerTest,
                resourcesRootName = "importStatementsData",
                inExtension = Extension.PY,
                outExtension = Extension.TXT,
            )
    }

    @Test
    fun testImportStatementsFqNamesInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)

        val actualImportStatementFqNames = inPsiFile.extractElementsOfType(PyImportStatement::class.java)
            .map { ImportStatementPsiAnalyzer.analyze(it) }.flatten().sorted()

        val expectedImportStatementFqNames = outFile!!.readLines().sorted()

        Assert.assertEquals(expectedImportStatementFqNames, actualImportStatementFqNames)
    }
}
