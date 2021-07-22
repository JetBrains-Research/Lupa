package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
open class RangesAnalysisExecutorTest :
    ParametrizedBaseTest(getResourcesRootPath(::RangesAnalysisExecutorTest)) {

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
            ::RangesAnalysisExecutorTest,
            inExtension = Extension.KT,
            outExtension = Extension.CSV
        )
    }

    @Test
    fun testRangesInProject() {
        val psiFile = getPsiFile(inFile!!, myFixture)

        val resultDir = Paths.get(getResourcesRootPath(::RangesAnalysisExecutorTest))
        val resultFileName = "result.csv"
        val resultFile = File(resultDir.toFile(), resultFileName)

        val analysisExecutor = RangesAnalysisExecutor(resultDir, rangesFilename = resultFileName)
        analysisExecutor.controlledResourceManagers.forEach { it.init() }
        analysisExecutor.analyse(psiFile.project)
        analysisExecutor.controlledResourceManagers.forEach { it.close() }

        val expectedRangesStatistics = csvToMap(outFile!!)
        val actualRangesStatistics = csvToMap(resultFile).toMutableMap()
        actualRangesStatistics.remove("project")
        Assert.assertEquals(expectedRangesStatistics, actualRangesStatistics)
    }

    private fun csvToMap(file: File, separator: String = "\t"): Map<String, MutableList<String>> {
        val lines = file.readLines()
        val headerNames = lines[0].split(separator)
        val csvAsMap: Map<String, MutableList<String>> = headerNames.associateWith { mutableListOf() }
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                line.split(separator).zip(headerNames).forEach { (cell, columnName) ->
                    csvAsMap[columnName]!!.add(cell)
                }
            }
        }
        return csvAsMap
    }
}
