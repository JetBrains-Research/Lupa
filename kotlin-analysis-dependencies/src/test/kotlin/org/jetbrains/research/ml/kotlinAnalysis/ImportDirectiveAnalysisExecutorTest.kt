package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.util.Extension
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedBaseTest
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
            inExtension = Extension.DIR,
            outExtension = Extension.TXT
        )
    }

    @Test
    fun testImportDirectiveFqNamesInProject() {
        val outputDir = Paths.get(getResourcesRootPath(::ImportDirectiveAnalysisExecutorTest))
        val outputFileName = "expected.txt"
        val outputFile = File(outputDir.toFile(), outputFileName)

        val analysisExecutor = ImportDirectivesAnalysisExecutor(outputDir, outputFileName)
        analysisExecutor.execute(inFile!!.toPath())

        val expectedImportDirectiveFqNames = outFile!!.readLines().sorted()
        val actualImportDirectiveFqNames = outputFile.readLines().sorted()
        outputFile.delete()

        Assert.assertEquals(expectedImportDirectiveFqNames.size, actualImportDirectiveFqNames.size)
        expectedImportDirectiveFqNames.zip(actualImportDirectiveFqNames).forEach { (expectedFqName, actualFqName) ->
            Assert.assertEquals(expectedFqName, actualFqName)
        }
    }
}
