package org.jetbrains.research.ml.kotlinAnalysis.tagging

import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
open class AndroidProjectTaggingTest :
    ParametrizedBaseTest(getResourcesRootPath(::AndroidProjectTaggingTest)) {

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
            ::AndroidProjectTaggingTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        val resultDir = Paths.get(getResourcesRootPath(::AndroidProjectTaggingTest))
        val resultFileName = "result.txt"
        val resultFile = File(resultDir.toFile(), resultFileName)

        val analysisExecutor = ProjectsTaggingExecutor(resultDir, resultFileName)
        analysisExecutor.execute(inFile!!.toPath())

        val expectedGradleDependencies = outFile!!.readLines().sorted()
        val actualGradleDependencies = resultFile.readLines().sorted()
        resultFile.delete()

        Assert.assertEquals(expectedGradleDependencies, actualGradleDependencies)
    }
}
