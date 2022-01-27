package org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.buildGradle.dependencies

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.GradleDependenciesAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class ExtractGradleDependenciesTest :
    ParametrizedGoldenFileTest(getResourcesRootPath(::ExtractGradleDependenciesTest)) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ExtractGradleDependenciesTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testGradleDependenciesExtraction() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor =
                GradleDependenciesAnalysisExecutor(outFile.parentFile.toPath(), filename = outFile.name)
            analysisExecutor.execute(inFile.toPath()) { null }
        }
    }
}
