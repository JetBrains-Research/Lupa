package org.jetbrains.research.ml.kotlinAnalysis.psi.extensions.project.modules

import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractModules
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractRootModule
import org.jetbrains.research.ml.kotlinAnalysis.util.ProjectSetupUtil
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Ignore("Plugin utils for now do not provide methods for opening project in tests")
@RunWith(Parameterized::class)
open class PsiExtractProjectModulesTest : ParametrizedBaseTest(getResourcesRootPath(::PsiExtractProjectModulesTest)) {

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
            ::PsiExtractProjectModulesTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractAllModulesFromProject() {
        val project = ProjectSetupUtil.setUpProject(inFile!!.toPath())
        val actualModuleNames = project!!.extractModules().map { it.name }
        val expectedModuleNames = outFile!!.readLines().sorted()
        Assert.assertEquals(actualModuleNames, expectedModuleNames)
    }

    @Test
    fun testExtractRootModuleFromProject() {
        val project = ProjectSetupUtil.setUpProject(inFile!!.toPath())
        val actualRootModuleName = project!!.extractRootModule()!!.name
        val expectedRootModuleName = outFile!!.readLines()[0]
        Assert.assertEquals(actualRootModuleName, expectedRootModuleName)
    }
}
