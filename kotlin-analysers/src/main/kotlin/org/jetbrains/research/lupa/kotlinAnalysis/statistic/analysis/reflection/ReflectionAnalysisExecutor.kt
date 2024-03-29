package org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.reflection

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractKtElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.KOTLIN_EXTENSIONS
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

/**
 * Executor for reflections analysis which collects projects that use java reflection functions
 * and stores them to the file in [output directory][outputDir].
 */
class ReflectionAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    reflectionFilename: String = "reflection_data.csv",
) : AnalysisExecutor(executorHelper, repositoryOpener) {

    private val reflectionDataWriter = PrintWriterResourceManager(outputDir, reflectionFilename, "project")
    override val controlledResourceManagers: Set<ResourceManager> = setOf(reflectionDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = KOTLIN_EXTENSIONS

    override fun analyse(project: Project) {
        val binaryExpressionReflections = project.extractKtElementsOfType(KtNameReferenceExpression::class.java)
        val binaryExpressionResults =
            binaryExpressionReflections.associateWith { ReferenceExpressionReflectionPsiAnalyzer.analyze(it) }
                .filter { it.value != JavaReflectionFunction.NOT_REFLECTION }

        if (binaryExpressionResults.isNotEmpty()) {
            reflectionDataWriter.writer.println(project.name)
        }
    }
}
