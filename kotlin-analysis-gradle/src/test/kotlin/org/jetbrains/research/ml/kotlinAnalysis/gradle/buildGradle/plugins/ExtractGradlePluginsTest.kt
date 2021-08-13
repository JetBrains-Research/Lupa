package org.jetbrains.research.ml.kotlinAnalysis.gradle.buildGradle.plugins

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradlePluginsAnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ExtractGradlePluginsTest :
    ParametrizedGoldenFileTest(getResourcesRootPath(::ExtractGradlePluginsTest)) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ExtractGradlePluginsTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testGradlePluginsExtraction() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor = GradlePluginsAnalysisExecutor(outFile.parentFile.toPath(), outFile.name)
            analysisExecutor.execute(inFile.toPath())
        }
    }
}
