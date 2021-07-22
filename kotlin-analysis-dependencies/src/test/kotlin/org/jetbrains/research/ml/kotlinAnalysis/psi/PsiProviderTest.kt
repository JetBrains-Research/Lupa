package org.jetbrains.research.ml.kotlinAnalysis.psi

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class PsiProviderTest : ParametrizedBaseTest(getResourcesRootPath(::PsiProviderTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::PsiProviderTest, outExtension = Extension.TXT)
    }

    @Test
    fun testImportDirectiveFqNamesInFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val actualImportDirectiveFqNames = PsiTreeUtil.collectElementsOfType(inPsiFile, KtImportDirective::class.java)
            .map { it.importedFqName.toString() }.sorted()
        val expectedImportDirectiveFqNames = outFile!!.readLines().sorted()

        Assert.assertEquals(expectedImportDirectiveFqNames, actualImportDirectiveFqNames)
    }
}
