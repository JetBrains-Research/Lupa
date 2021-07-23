package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
open class ImportDirectiveAnalysisExecutorTest :
    ParametrizedBaseTest(getResourcesRootPath(::ImportDirectiveAnalysisExecutorTest)) {

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
            ::ImportDirectiveAnalysisExecutorTest,
            inExtension = Extension.EMPTY,
            outExtension = Extension.TXT
        )
    }

    @Test
    fun testImportDirectiveFqNamesInProject() {
        val resultDir = Paths.get(getResourcesRootPath(::ImportDirectiveAnalysisExecutorTest))
        val resultFileName = "result.txt"
        val resultFile = File(resultDir.toFile(), resultFileName)

        val analysisExecutor = ImportDirectivesAnalysisExecutor(resultDir, resultFileName)
        analysisExecutor.execute(inFile!!.toPath())

        val expectedImportDirectiveFqNames = outFile!!.readLines().sorted()
        val actualImportDirectiveFqNames = resultFile.readLines().sorted()
        resultFile.delete()

        Assert.assertEquals(expectedImportDirectiveFqNames, actualImportDirectiveFqNames)
    }
}
