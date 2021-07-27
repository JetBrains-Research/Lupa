package org.jetbrains.research.ml.kotlinAnalysis.psi

import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class ImportDirectiveExtractionTest : ParametrizedBaseTest(getResourcesRootPath(::ImportDirectiveExtractionTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::ImportDirectiveExtractionTest, outExtension = Extension.TXT)
    }

    @Test
    fun testImportDirectiveFqNamesInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val actualImportDirectiveFqNames = inPsiFile.extractElementsOfType(KtImportDirective::class.java)
            .map { it.importedFqName.toString() }.sorted()
        val expectedImportDirectiveFqNames = outFile!!.readLines().sorted()

        Assert.assertEquals(expectedImportDirectiveFqNames, actualImportDirectiveFqNames)
    }
}
