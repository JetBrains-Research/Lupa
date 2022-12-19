package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.gradleProperties

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.GradlePropertiesAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ExtractGradlePropertiesTest :
    ParametrizedGoldenFileTest(getResourcesRootPath(::ExtractGradlePropertiesTest)) {

    override fun runInDispatchThread() = false

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ExtractGradlePropertiesTest,
            inExtension = Extension.EMPTY,
            outExtension = Extension.TXT,
        )
    }

    @Test
    fun testGradlePropertiesExtraction() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor =
                GradlePropertiesAnalysisExecutor(outFile.parentFile.toPath(), filename = outFile.name)
            analysisExecutor.execute(inFile.toPath()) { null }
        }
    }
}
