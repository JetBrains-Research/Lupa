package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ImportDirectiveAnalysisExecutorTest :
    ParametrizedGoldenFileTest(getResourcesRootPath(::ImportDirectiveAnalysisExecutorTest)) {

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
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor = ImportDirectivesAnalysisExecutor(outFile.parentFile.toPath(), outFile.name)
            analysisExecutor.execute(inFile.toPath())
        }
    }
}
