package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.io.File

enum class Type {
    Input, Output
}

class TestFileFormat(private val prefix: String, private val extension: Extension, val type: Type) {
    data class TestFile(val file: File, val type: Type, val number: Number)

    fun check(file: File): TestFile? {
        val regex = if (extension == Extension.EMPTY) {
            "(?<=${prefix}_)\\d+(?=(_.*)?)".toRegex()
        } else {
            "(?<=${prefix}_)\\d+(?=(_.*)?\\.${extension.value})".toRegex()
        }
        val number = regex.find(file.name)?.value?.toInt()
        return number?.let { TestFile(file, type, number) }
    }

    fun match(testFile: TestFile): Boolean {
        return testFile.type == type
    }
}

object FileTestUtil {

    /**
     * We assume the format of the test files will be:
     *
     * inPrefix_i_anySuffix[.inExtension]?
     * outPrefix_i_anySuffix[.outExtension?,
     *
     * where:
     * inPrefix and outPrefix are set in [inFormat] and [outFormat] together with extensions,
     * i is a number; two corresponding input and output files should have the same number,
     * suffixes can by any symbols not necessary the same for the corresponding files,
     * extensions listed in [Extension] are optional(empty) only for directories.
     */
    fun getInAndOutFilesMap(
        folder: String,
        inFormat: TestFileFormat = TestFileFormat("in", Extension.KT, Type.Input),
        outFormat: TestFileFormat? = null
    ): Map<File, File?> {
        val files = File(folder).listFiles().orEmpty()
        // Partition files in the given folder: which match in/out format and which will be processed recursively
        val inAndOutFileFormats = mutableListOf<TestFileFormat.TestFile>()
        val toProcessFiles = mutableListOf<File>()

        files.forEach { file ->
            val fileFormat = inFormat.check(file) ?: outFormat?.check(file)
            fileFormat?.let { inAndOutFileFormats.add(it) } ?: toProcessFiles.add(file)
        }

        val inAndOutFilesMap = inAndOutFileFormats
            .groupBy { it.number }
            .map { (number, fileInfoList) ->
                val (f1, f2) = if (outFormat == null) {
                    require(fileInfoList.size == 1) { "There are less or more than 1 test files with number $number" }
                    Pair(fileInfoList.first(), null)
                } else {
                    require(fileInfoList.size == 2) { "There are less or more than 2 test files with number $number" }
                    fileInfoList.sortedBy { it.type }.zipWithNext().first()
                }
                require(inFormat.match(f1)) { "The input file does not match the input format" }
                outFormat?.let {
                    require(f2 != null && outFormat.match(f2)) { "The output file does not match the output format" }
                }
                f1.file to f2?.file
            }.sortedBy { it.first.name }.toMap()

        outFormat?.let {
            require(inAndOutFilesMap.values.mapNotNull { it }.size == inAndOutFilesMap.values.size) { "Output tests" }
        }

        return toProcessFiles.filter { it.isDirectory }.sortedBy { it.name }
            .map { getInAndOutFilesMap(it.absolutePath, inFormat, outFormat) }
            .fold(inAndOutFilesMap) { a, e -> a.plus(e) }
    }
}

fun getPsiFile(file: File, fixture: CodeInsightTestFixture): PsiFile {
    return fixture.configureByFile(file.path)
}
