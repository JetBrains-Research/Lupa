package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import java.nio.file.Path

/**
 * Extracts all methods from projects in the dataset and saves methods in Clone Detection Tool format.
 */
class FormattedMethodMiner(outputDir: Path) {
    private val indexer = IndexBuilder(outputDir)
    private val methodDataWriter = printWriter(outputDir, "method_data.txt")

    fun extractMethodsToCloneDetectionFormat(inputDir: Path) {
        getSubdirectories(inputDir).forEach { projectPath ->
            ProjectUtil.openOrImport(projectPath, null, true).let { project ->
                val projectIndex = indexer.indexProject(project)
                val methods = PsiProvider.extractMethodsFromProject(project)
                val methodsIndexed = methods.associateWith { method ->
                    indexer.indexMethod(method, projectIndex)
                }
                methodsIndexed.forEach { (method, methodIndex) ->
                    val methodFormatted = CloneDetectionAdapter.format(method, projectIndex, methodIndex)
                    methodDataWriter.println(methodFormatted)
                }
            }
        }
        indexer.close()
        methodDataWriter.close()
    }
}
