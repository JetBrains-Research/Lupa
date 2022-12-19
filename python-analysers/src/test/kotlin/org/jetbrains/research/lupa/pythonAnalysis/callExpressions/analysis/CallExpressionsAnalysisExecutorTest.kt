package org.jetbrains.research.lupa.pythonAnalysis.callExpressions.analysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.lupa.kotlinAnalysis.util.ParametrizedGoldenFileTest
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@Ignore
@RunWith(Parameterized::class)
open class CallExpressionsAnalysisExecutorTest : ParametrizedGoldenFileTest(
    getResourcesRootPath(
        ::CallExpressionsAnalysisExecutorTest,
        "callExpressionsAnalysisExecutorData",
    ),
) {
    private lateinit var sdk: Sdk

    override fun runInDispatchThread() = false

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::CallExpressionsAnalysisExecutorTest,
            "callExpressionsAnalysisExecutorData",
            inExtension = Extension.EMPTY,
            outExtension = Extension.CSV,
        )
    }

    private fun setupSdk(project: Project) {
        ApplicationManager.getApplication().invokeAndWait {
            val projectManager = ProjectRootManager.getInstance(project)
            sdk = PythonMockSdk(testDataPath).create("3.8")
            val sdkConfigurer = SdkConfigurer(project, projectManager)
            sdkConfigurer.setProjectSdk(sdk)
        }
    }

    override fun tearDown() {
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(sdk)
            }
        }
        super.tearDown()
    }

    @Test
    fun testCallExpressionsFqNamesInProject() {
        assertOutEqualsToGolden { inFile, outFile ->
            val analysisExecutor =
                CallExpressionsAnalysisExecutor(outFile.parentFile.toPath(), filename = outFile.name, venv = null)

            analysisExecutor.controlledResourceManagers.map { it.init() }

            RepositoryOpenerUtil.standardRepositoryOpener(inFile.toPath()) { project ->
                setupSdk(project)
                analysisExecutor.analyseWithResult(project)
            }

            analysisExecutor.controlledResourceManagers.map { it.close() }
        }
    }
}
