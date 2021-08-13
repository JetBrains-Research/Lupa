package org.jetbrains.research.ml.kotlinAnalysis.util

import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

/**
 * Class for tests where exists [function][fileProducer], which produces a [file][outFile] by the [given file][inFile].
 * Test for this function is consists of comparing produced [file][outFile] with the [golden file][goldenFile] one.
 */
@Ignore
open class ParametrizedGoldenFileTest(private val testDataRoot: String) : ParametrizedBaseTest(testDataRoot) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var goldenFile: File? = null

    /** Asserts that file [outFile], produced by [fileProducer] based on [inFile] is equals to [goldenFile]. */
    fun assertOutEqualsToGolden(fileProducer: (File, File) -> Unit) {
        val outFile = File(Paths.get(testDataRoot).toFile(), "out.tmp")

        fileProducer(inFile!!, outFile)

        val goldenFileContent = goldenFile!!.readLines().sorted()
        val outFileContent = outFile.readLines().sorted()
        outFile.delete()

        Assert.assertEquals(goldenFileContent, outFileContent)
    }
}
