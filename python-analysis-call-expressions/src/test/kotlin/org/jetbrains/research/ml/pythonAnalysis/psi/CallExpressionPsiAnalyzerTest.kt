package org.jetbrains.research.ml.pythonAnalysis.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzer
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzerContext
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class CallExpressionPsiAnalyzerTest : ParametrizedBaseTest(
    getResourcesRootPath(
        ::CallExpressionPsiAnalyzerTest,
        resourcesRootName = "callExpressionPsiAnalyzerTestData"
    )
) {

    private lateinit var sdk: Sdk

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() =
            getInAndOutArray(
                ::CallExpressionPsiAnalyzerTest,
                resourcesRootName = "callExpressionPsiAnalyzerTestData",
                inExtension = Extension.PY,
                outExtension = Extension.TXT,
            )
    }

    private val VENV_SCRIPT_PATH = "../scripts/plugin_runner/create_venv.py"
    private val VENV_PATH = File("$testDataPath/venv/")

//    private fun createVirtualEnvSdk(project: Project, baseSdk: Sdk, venvRoot: String): Sdk {
//        var sdk: Sdk? = null
//        ApplicationManager.getApplication().invokeAndWait {
//            sdk = PyProjectVirtualEnvConfiguration.createVirtualEnvSynchronously(
//                baseSdk = baseSdk,
//                existingSdks = listOf(baseSdk),
//                venvRoot = venvRoot,
//                projectBasePath = project.basePath,
//                project = project,
//                module = null
//            )
//        }
//        return sdk ?: error("Internal error: SDK for ${project.name} project was not created")
//    }

    private fun setupSdk() {
        val project = myFixture.project
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(testDataPath).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }

    override fun setUp() {
        super.setUp()

        VENV_PATH.mkdir()

        val command = arrayOf(
            "python3",
            File(VENV_SCRIPT_PATH).absolutePath,
            getResourcesRootPath(::CallExpressionPsiAnalyzerTest, "callExpressionPsiAnalyzerTestData"),
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

        VENV_PATH.deleteRecursively()
    }

    @Test
    fun testCallExpressionFqNamesInFile() {
        val typeEvalContext = TypeEvalContext.deepCodeInsight(myFixture.project)
        val pyResolveContext = PyResolveContext.defaultContext(typeEvalContext)
        val fqNamesProvider = PyQualifiedNameProvider()

        val analyzerContext = CallExpressionAnalyzerContext(pyResolveContext, fqNamesProvider)

        val inPsiFile = getPsiFile(inFile!!, myFixture)

        val callExpressions =
            inPsiFile.extractElementsOfType(PyCallExpression::class.java).filter { it !is PyDecorator }

        val actualCallExpressionFqNames =
            callExpressions.map { CallExpressionAnalyzer.analyze(it, analyzerContext) }

        val expectedCallExpressionFqNames = outFile!!.readLines().sorted()

        assertEquals(actualCallExpressionFqNames, expectedCallExpressionFqNames)
    }
}
