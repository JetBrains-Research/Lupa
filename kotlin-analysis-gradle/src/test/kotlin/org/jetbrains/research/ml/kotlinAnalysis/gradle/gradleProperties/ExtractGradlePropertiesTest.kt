package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties

import org.jetbrains.research.ml.kotlinAnalysis.GradlePropertiesAnalysisExecutor
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
open class ExtractGradlePropertiesTest :
    ParametrizedBaseTest(getResourcesRootPath(::ExtractGradlePropertiesTest)) {

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
            ::ExtractGradlePropertiesTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        val resultDir = Paths.get(getResourcesRootPath(::ExtractGradlePropertiesTest))
        val resultFileName = "result.txt"
        val resultFile = File(resultDir.toFile(), resultFileName)

        val analysisExecutor = GradlePropertiesAnalysisExecutor(resultDir, resultFileName)
        analysisExecutor.execute(inFile!!.toPath())

        val expectedGradleProperties = outFile!!.readLines().sorted()
        val actualGradleProperties = resultFile.readLines().sorted()
        resultFile.delete()

        Assert.assertEquals(expectedGradleProperties, actualGradleProperties)
    }
}
