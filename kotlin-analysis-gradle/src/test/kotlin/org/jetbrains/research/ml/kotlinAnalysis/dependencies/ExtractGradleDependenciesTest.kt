package org.jetbrains.research.ml.kotlinAnalysis.dependencies

import org.jetbrains.research.ml.kotlinAnalysis.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.util.ProjectSetupUtil
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class ExtractGradleDependenciesTest :
    ParametrizedBaseTest(getResourcesRootPath(::ExtractGradleDependenciesTest)) {

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
            ::ExtractGradleDependenciesTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        val project = ProjectSetupUtil.setUpProject(inFile!!.toPath())
        val rootGradlePsiFile = GradleFileManager.extractRootGradleFileFromProject(project!!)!!
        val actualGradleDependencies = rootGradlePsiFile.extractBuildGradleDependencies().map { it.toString() }.sorted()
        val expectedGradleDependencies = outFile!!.readLines().sorted()
        Assert.assertEquals(actualGradleDependencies, expectedGradleDependencies)
    }
}
