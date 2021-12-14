package org.jetbrains.research.ml.pythonAnalysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.ml.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

@RunWith(Parameterized::class)
open class CallExpressionsAnalysisExecutorTest : ParametrizedGoldenFileTest(
    getResourcesRootPath(
        ::CallExpressionsAnalysisExecutorTest,
        "callExpressionsAnalysisExecutorData"
    )
) {
    private lateinit var sdk: Sdk

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::CallExpressionsAnalysisExecutorTest,
            "callExpressionsAnalysisExecutorData",
            inExtension = Extension.EMPTY,
            outExtension = Extension.CSV
        )

        private const val VENV_SCRIPT_PATH = "../scripts/plugin_runner/create_venv.py"
        private val VENV_PATH = createTempDirectory().toAbsolutePath()
    }

    private fun setupSdk() {
        val project = myFixture.project
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(testDataPath).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }

    override fun setUp() {
        super.setUp()

        val command = arrayOf(
            "python3",
            File(VENV_SCRIPT_PATH).absolutePath,
            getResourcesRootPath(::CallExpressionsAnalysisExecutorTest, "callExpressionsAnalysisExecutorData"),
            VENV_PATH.toString(),
        )

        ProcessBuilder(*command)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)

        setupSdk()
    }

    override fun tearDown() {
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
        super.tearDown()
    }

    @Test
    fun testCallExpressionsFqNamesInProject() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor =
                CallExpressionsAnalysisExecutor(outFile.parentFile.toPath(), outFile.name, venv = VENV_PATH)
            analysisExecutor.execute(inFile.toPath())
        }
    }
}
