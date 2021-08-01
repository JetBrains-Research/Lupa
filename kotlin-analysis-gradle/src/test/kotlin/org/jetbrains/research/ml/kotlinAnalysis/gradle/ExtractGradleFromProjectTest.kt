package org.jetbrains.research.ml.kotlinAnalysis.gradle

import org.jetbrains.research.ml.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
open class ExtractGradleFromProjectTest :
    ParametrizedBaseTest(getResourcesRootPath(::ExtractGradleFromProjectTest)) {

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
            ::ExtractGradleFromProjectTest,
            inExtension = Extension.EMPTY, outExtension = Extension.TXT
        )
    }

    @Test
    fun testExtractGradleFilesFromProject() {
        RepositoryOpenerUtil.openReloadRepositoryOpener(inFile!!.toPath()) { project ->
            val actualGradleFilePaths = GradleFileManager
                .extractGradleFilesFromProject(project).map { it.parent!!.name }.sorted()
            val expectedGradleFilePaths = outFile!!.readLines().sorted()
            Assert.assertEquals(expectedGradleFilePaths, actualGradleFilePaths)
        }
    }

    @Test
    fun testExtractRootGradleFileFromProject() {
        RepositoryOpenerUtil.openReloadRepositoryOpener(inFile!!.toPath()) { project ->
            val actualRootGradleFilePath = GradleFileManager
                .extractRootBuildGradleFileFromProject(project)!!.parent!!.name
            val expectedGradleFilePaths = outFile!!.readLines()[0]
            Assert.assertEquals(expectedGradleFilePaths, actualRootGradleFilePath)
        }
    }
}
