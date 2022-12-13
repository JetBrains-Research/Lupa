package org.jetbrains.research.lupa.pythonAnalysis.imports.analysis

import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ImportStatementsAnalysisExecutorTest : ParametrizedGoldenFileTest(
    getResourcesRootPath(
        ::ImportStatementsAnalysisExecutorTest,
        "importStatementsAnalysisExecutorData",
    ),
) {
    override fun runInDispatchThread() = false

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ImportStatementsAnalysisExecutorTest,
            "importStatementsAnalysisExecutorData",
            inExtension = Extension.EMPTY,
            outExtension = Extension.CSV,
        )
    }

    @Test
    fun testImportStatementsFqNamesInProject() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor =
                ImportStatementsAnalysisExecutor(outFile.parentFile.toPath(), filename = outFile.name)
            analysisExecutor.execute(inFile.toPath()) { null }
        }
    }
}
