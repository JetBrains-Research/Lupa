package org.jetbrains.research.ml.kotlinAnalysis.gradle.gradleProperties

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradlePropertiesAnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ExtractGradlePropertiesTest :
    ParametrizedGoldenFileTest(getResourcesRootPath(::ExtractGradlePropertiesTest)) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ExtractGradlePropertiesTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testGradlePropertiesExtraction() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor = GradlePropertiesAnalysisExecutor(outFile.parentFile.toPath(), outFile.name)
            analysisExecutor.execute(inFile.toPath())
        }
    }
}
