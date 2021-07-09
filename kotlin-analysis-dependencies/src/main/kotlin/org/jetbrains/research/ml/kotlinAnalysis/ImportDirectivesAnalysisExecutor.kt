package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import java.nio.file.Path

/**
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportDirectivesAnalysisExecutor(outputDir: Path, filename: String = "import_directives_data.csv") :
    AnalysisExecutor() {

    private val dependenciesDataWriter = PrintWriterResourceManager(outputDir, filename)
    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val importDirectives = PsiProvider.extractElementsOfTypeFromProject(project, KtImportDirective::class.java)
        val results = importDirectives.map { ImportDirectivePsiAnalyzer.analyze(it) }
        if (results.isNotEmpty()) {
            dependenciesDataWriter.writer.println(results.joinToString(separator = "\n"))
        }
    }
}
