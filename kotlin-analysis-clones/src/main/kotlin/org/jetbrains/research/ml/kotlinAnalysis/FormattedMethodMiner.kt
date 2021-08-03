package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.deleteComments
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import java.nio.file.Path

/**
 * Extracts all methods from projects in the dataset and saves methods in Clone Detection Tool
 * ([SourcererCC](https://github.com/JetBrains-Research/SourcererCC/)) format.
 */
class FormattedMethodMiner(outputDir: Path) : AnalysisExecutor() {

    private val methodDataWriter = PrintWriterResourceManager(outputDir, "method_data.txt")
    private val indexer = IndexBuilder(outputDir)
    override val controlledResourceManagers: Set<ResourceManager> = setOf(methodDataWriter, indexer)

    override fun analyse(project: Project) {
        val projectIndex = indexer.indexProject(project)
        println("Start processing project ${project.name} (index $projectIndex)")
        // We should reverse the list with method since we handle all elements separately
        // and we can invalidate the previous one
        val methods = project
            .extractElementsOfType(KtNamedFunction::class.java)
            .reversed()
        val methodsIndexed = methods.associateWith { method ->
            indexer.indexMethod(method, projectIndex)
        }
        methodsIndexed.forEach { (method, methodIndex) ->
            val methodFormatted = CloneDetectionAdapter.format(
                method,
                projectIndex,
                methodIndex,
                PsiElement::deleteComments
            )
            methodDataWriter.writer.println(methodFormatted)
        }
    }
}
