package org.jetbrains.research.ml.kotlinAnalysis.psi

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
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
        val inMethods = PsiTreeUtil.collectElementsOfType(inPsiFile, KtNamedFunction::class.java)
        val outMethods = PsiTreeUtil.collectElementsOfType(outPsiFile, KtNamedFunction::class.java)
        Assert.assertEquals(outMethods.size, inMethods.size)
        inMethods.zip(outMethods).forEach { (inMethod, outMethod) ->
            PsiProvider.deleteComments(inMethod)
            PsiProvider.deleteComments(outMethod)
            Assert.assertEquals(inMethod.text, outMethod.text)
        }
    }
}
