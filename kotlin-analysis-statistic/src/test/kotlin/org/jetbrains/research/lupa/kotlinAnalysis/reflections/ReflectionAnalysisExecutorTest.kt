package org.jetbrains.research.lupa.kotlinAnalysis.reflections

import org.jetbrains.research.lupa.kotlinAnalysis.reflection.ReflectionAnalysisExecutor
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
class ReflectionAnalysisExecutorTest :
    ParametrizedBaseTest(getResourcesRootPath(::ReflectionAnalysisExecutorTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ReflectionAnalysisExecutorTest,
            inExtension = Extension.KT,
            outExtension = Extension.CSV
        )
    }

    @Test
    fun testReflectionsFunctionsInProject() {
        val psiFile = getPsiFile(inFile!!, myFixture)

        val resultDir = Paths.get(getResourcesRootPath(::ReflectionAnalysisExecutorTest))
        val resultFileName = "result.csv"
        val resultFile = File(resultDir.toFile(), resultFileName)

        val analysisExecutor = ReflectionAnalysisExecutor(resultDir, reflectionFilename = resultFileName)
        analysisExecutor.controlledResourceManagers.forEach { it.init() }
        analysisExecutor.analyse(psiFile.project)
        analysisExecutor.controlledResourceManagers.forEach { it.close() }

        Assert.assertEquals(outFile!!.countLines(), resultFile.countLines())
    }

    private fun File.countLines() = this.readLines().size
}
