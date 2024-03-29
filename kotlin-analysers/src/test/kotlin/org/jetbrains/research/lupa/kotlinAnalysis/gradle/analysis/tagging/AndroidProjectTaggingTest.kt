package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.tagging

import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class AndroidProjectTaggingTest : ParametrizedGoldenFileTest(getResourcesRootPath(::AndroidProjectTaggingTest)) {

    override fun runInDispatchThread() = false

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::AndroidProjectTaggingTest,
            inExtension = Extension.EMPTY,
            outExtension = Extension.TXT,
        )
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor = ProjectsTaggingExecutor(outFile.parentFile.toPath(), filename = outFile.name)
            analysisExecutor.execute(inFile.toPath()) { null }
        }
    }
}
