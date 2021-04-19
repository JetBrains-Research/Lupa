package org.jetbrains.research.ml.kotlinAnalysis.psi

import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedBaseTest
import org.jetbrains.research.ml.kotlinAnalysis.util.getPsiFile
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
        fun getTestData() = getInAndOutArray(::PsiProviderTest)
    }

    @Test
    fun testDeleteComments() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        PsiProvider.deleteComments(inPsiFile)
        Assert.assertEquals(inPsiFile.text, outPsiFile.text)
    }
}
