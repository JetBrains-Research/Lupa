package org.jetbrains.research.lupa.kotlinAnalysis.clones.analysis

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.deleteComments
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import java.nio.file.Path

/**
 * Extracts all methods from projects in the dataset and saves methods in Clone Detection Tool
 * ([SourcererCC](https://github.com/JetBrains-Research/SourcererCC/)) format.
 */
class FormattedMethodMiner(outputDir: Path, executorHelper: ExecutorHelper? = null) : AnalysisExecutor(executorHelper) {

    private val methodDataWriter = PrintWriterResourceManager(outputDir, "method_data.txt")
    private val indexer = IndexBuilder(outputDir)
    override val controlledResourceManagers: Set<ResourceManager> = setOf(methodDataWriter, indexer)

    override fun analyse(project: Project) {
        val projectIndex = indexer.indexProject(project)
        println("Start processing project ${project.name} (index $projectIndex)")
        // We should reverse the list with method since we handle all elements separately
        // and we can invalidate the previous one
        val methods = project
            .extractKtElementsOfType(KtNamedFunction::class.java)
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
