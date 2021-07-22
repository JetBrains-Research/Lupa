package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
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
        fun getTestData() = getInAndOutArray(::CloneDetectionAdapterTest, outExtension = Extension.TXT)
    }

    @Test
    fun testFormat() {
        val psiFile = getPsiFile(inFile!!, myFixture)
        val result = outFile!!.readLines()[0]
        val methods = PsiTreeUtil.collectElementsOfType(psiFile, KtNamedFunction::class.java)
        Assert.assertEquals(1, methods.size)
        methods.forEach {
            val formattedMethod = CloneDetectionAdapter.format(it, 0, 0)
            assertStatsEquals(formattedMethod, result)
            assertTokensEquals(formattedMethod, result)
        }
    }

    private fun assertStatsEquals(formattedMethod: String, result: String) {
        val statsIn = statsFromAdapterOutput(formattedMethod)
        val statsOut = statsFromAdapterOutput(result)
        Assert.assertEquals(statsOut, statsIn)
    }

    private fun assertTokensEquals(formattedMethod: String, result: String) {
        val counterIn = counterFromAdapterOutput(formattedMethod)
        val counterOut = counterFromAdapterOutput(result)
        Assert.assertEquals(counterOut, counterIn)
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
