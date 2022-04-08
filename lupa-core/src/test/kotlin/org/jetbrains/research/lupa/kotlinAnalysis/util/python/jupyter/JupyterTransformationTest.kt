package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

fun getNotebookTransformationString(file: File): String {
    val filename = file.toString()
    val notebookJSON: JsonObject = JsonParser.parseString(readFileAsText(filename)).asJsonObject
    val notebookName = file.nameWithoutExtension
    val ntb = Notebook(notebookJSON, notebookName)
    return ntb.transformNotebookToString()
}

fun getOutputScriptString(file: File) = readFileAsText(file.toString())

@RunWith(Parameterized::class)
open class JupyterTransformationTest : ParametrizedBaseTest(getResourcesRootPath(::JupyterTransformationTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::JupyterTransformationTest,
            inExtension = Extension.TXT, outExtension = Extension.PY)
    }

    @Test
    fun testJupyterTransformationToPythonScript() {
        val inNotebookAsScriptString = getNotebookTransformationString(inFile!!)
        val outPythonScriptString = getOutputScriptString(outFile!!)
        val blankLine = "${System.lineSeparator()}${System.lineSeparator()}"
        Assert.assertEquals(
            inNotebookAsScriptString.replace("\n\n", System.lineSeparator()),
            outPythonScriptString.replace("\n\n", System.lineSeparator()))
    }
}
