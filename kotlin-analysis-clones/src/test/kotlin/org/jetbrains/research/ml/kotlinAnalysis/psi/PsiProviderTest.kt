package org.jetbrains.research.ml.kotlinAnalysis.psi

import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedBaseTest
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
        val psiFile1 = getPsiFile(inFile!!)
        val psiFile2 = getPsiFile(outFile!!)
        PsiProvider.deleteComments(psiFile1)
        Assert.assertEquals(psiFile1.text, psiFile2.text)
    }

    private fun getPsiFile(file: File): PsiFile {
        return myFixture.configureByFile(file.path)
    }
}
