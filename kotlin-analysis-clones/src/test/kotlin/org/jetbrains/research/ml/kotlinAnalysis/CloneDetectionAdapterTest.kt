package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.research.ml.kotlinAnalysis.util.Extension
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class CloneDetectionAdapterTest : ParametrizedBaseTest(getResourcesRootPath(::CloneDetectionAdapterTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::CloneDetectionAdapterTest, outExtension = Extension.Txt)
    }

    @Test
    fun testFormat() {
        val psiFile = getPsiFile(inFile!!)
        val result = outFile!!.readLines()[0]
        val methods = PsiTreeUtil.collectElementsOfType(psiFile, KtNamedFunction::class.java)
        Assert.assertEquals(methods.size, 1)
        methods.forEach {
            val formattedMethod = CloneDetectionAdapter.format(it, 0, 0)
            assertFormattedMethodsEquals(formattedMethod, result)
        }
    }

    private fun getPsiFile(file: File): PsiFile {
        return myFixture.configureByFile(file.path)
    }

    private fun assertFormattedMethodsEquals(formattedMethod: String, result: String) {
        assertStatsEquals(formattedMethod, result)
        assertTokensEquals(formattedMethod, result)
    }

    private fun assertStatsEquals(formattedMethod: String, result: String) {
        val statsIn = statsFromAdapterOutput(formattedMethod)
        val statsOut = statsFromAdapterOutput(result)
        Assert.assertEquals(statsIn, statsOut)
    }

    private fun assertTokensEquals(formattedMethod: String, result: String) {
        val counterIn = counterFromAdapterOutput(formattedMethod)
        val counterOut = counterFromAdapterOutput(result)
        Assert.assertEquals(counterIn, counterOut)
    }

    private fun statsFromAdapterOutput(methodStr: String): String {
        return methodStr.split("@#@")[0]
    }

    private fun counterFromAdapterOutput(methodStr: String): Map<String, Int> {
        return methodStr.split("@#@")[1].split(",").associate {
            val tokenCounter = it.split("@@::@@")
            tokenCounter[0] to tokenCounter[1].toInt()
        }
    }
}
