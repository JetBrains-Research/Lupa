package org.jetbrains.research.ml.kotlinAnalysis.psi

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.deleteComments
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedBaseTest
import org.jetbrains.research.ml.kotlinAnalysis.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class PsiFileExtensionTest : ParametrizedBaseTest(getResourcesRootPath(::PsiFileExtensionTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::PsiFileExtensionTest)
    }

    @Test
    fun testDeleteCommentsInWholeFile() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        inPsiFile.deleteComments()
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
            inMethod.deleteComments()
            outMethod.deleteComments()
            Assert.assertEquals(inMethod.text, outMethod.text)
        }
    }
}
