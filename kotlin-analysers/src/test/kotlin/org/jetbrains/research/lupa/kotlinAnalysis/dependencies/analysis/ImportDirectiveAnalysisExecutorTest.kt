package org.jetbrains.research.lupa.kotlinAnalysis.dependencies.analysis

import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
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
            val analysisExecutor =
                ImportDirectivesAnalysisExecutor(outFile.parentFile.toPath(), filename = outFile.name)
            analysisExecutor.executeAllProjects(inFile.toPath())
        }
    }
}
