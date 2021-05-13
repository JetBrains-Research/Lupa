package org.jetbrains.research.ml.kotlinAnalysis.psi

import org.jetbrains.research.ml.kotlinAnalysis.CloneDetectionAdapter
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
    fun testDeleteCommentsInWholeFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        PsiProvider.deleteComments(inPsiFile)
        Assert.assertEquals(outPsiFile.text, inPsiFile.text)
    }

    @Test
    fun testDeleteCommentsInMethods() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        val inMethods = PsiProvider.collectPsiMethods(inPsiFile)
        val outMethods = PsiProvider.collectPsiMethods(outPsiFile)
        Assert.assertEquals(outMethods.size, inMethods.size)
        val projectIndex = 0
        inMethods.zip(outMethods).forEachIndexed { index, (inMethod, outMethod) ->
            val inMethodFormatted =
                CloneDetectionAdapter.format(inMethod, projectIndex, index, PsiProvider::deleteComments)
            val outMethodFormatted =
                CloneDetectionAdapter.format(outMethod, projectIndex, index, PsiProvider::deleteComments)
            Assert.assertEquals(outMethodFormatted, inMethodFormatted)
        }
    }
}
