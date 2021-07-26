package org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.project

import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractRootModule
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.util.ProjectSetupUtil
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
open class PsiExtractGradleFromProjectTest :
    ParametrizedBaseTest(getResourcesRootPath(::PsiExtractGradleFromProjectTest)) {

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
            ::PsiExtractGradleFromProjectTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractGradleFilesFromProject() {
        val project = ProjectSetupUtil.setUpProject(inFile!!.toPath())
        val actualGradleFilePaths =
            GradleFileManager.extractGradleFilesFromProject(project!!).map { it.virtualFile.path }
        val expectedGradleFilePaths = outFile!!.readLines().sorted().map { Paths.get(name) }
        Assert.assertEquals(actualGradleFilePaths, expectedGradleFilePaths)
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        val project = ProjectSetupUtil.setUpProject(inFile!!.toPath())
        val actualRootModuleName = project!!.extractRootModule()!!.name
        val expectedRootModuleName = outFile!!.readLines()[0]
        Assert.assertEquals(actualRootModuleName, expectedRootModuleName)
    }
}
