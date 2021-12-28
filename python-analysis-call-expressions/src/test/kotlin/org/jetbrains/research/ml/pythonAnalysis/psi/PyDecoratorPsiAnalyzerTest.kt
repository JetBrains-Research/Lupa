package org.jetbrains.research.ml.pythonAnalysis.psi

import com.jetbrains.python.psi.PyDecorator
import org.jetbrains.research.ml.pythonAnalysis.PyDecoratorAnalyzer
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class PyDecoratorPsiAnalyzerTest : ParametrizedBaseWithPythonSdkTest(
    getResourcesRootPath(
        ::PyDecoratorPsiAnalyzerTest,
        resourcesRootName = "decoratorPsiAnalyzerTestData"
    )
) {
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
                ::PyDecoratorPsiAnalyzerTest,
                resourcesRootName = "decoratorPsiAnalyzerTestData",
                inExtension = Extension.PY,
                outExtension = Extension.TXT,
            )
    }

    @Test
    fun testPyDecoratorFqNamesInFile() {
        testCallExpressionFqNamesInFile(myFixture, inFile!!, outFile!!, PyDecoratorAnalyzer) { it is PyDecorator }
    }
}
