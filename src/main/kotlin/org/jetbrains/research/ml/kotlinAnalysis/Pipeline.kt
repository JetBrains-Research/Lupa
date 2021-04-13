package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class Pipeline(private val outputDir: Path) {
    private val indexer = IndexBuilder()
    private val psiProvider = PsiProvider()
    private val adapter = CloneDetectionAdapter()

    fun extractMethodsToCloneDetectionFormat(inputDir: Path) {
        val projectIndexWriter = File(outputDir.toFile(), "project_index.txt").printWriter()
        val methodIndexWriter = File(outputDir.toFile(), "method_index.csv").printWriter()
        val methodDataWriter = File(outputDir.toFile(), "method_data.txt").printWriter()

        Files.walk(inputDir, 1)
            .filter { Files.isDirectory(it) && !it.equals(inputDir) }
            .toArray()
            .forEach { projectPath ->
                val project = ProjectUtil.openOrImport(projectPath as Path, null, true) ?: return@forEach
                val projectIndex = indexer.indexProject(project, projectIndexWriter)
                val methods = psiProvider.extractMethodsFromProject(project)
                methods.forEach { method ->
                    val methodIndex = indexer.indexMethod(method, projectIndex, methodIndexWriter)
                    val methodFormatted = adapter.format(method, projectIndex, methodIndex)
                    methodDataWriter.println(methodFormatted)
                }
            }
        projectIndexWriter.close()
        methodIndexWriter.close()
        methodDataWriter.close()
    }
}
