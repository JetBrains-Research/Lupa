package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import java.nio.file.Path

/**
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportDirectivesAnalysisExecutor(outputDir: Path) : AnalysisExecutor() {

    private val dependenciesDataWriter = PrintWriterRecourse(outputDir, "import_directives_data.csv")
    override val controlledResources: Set<Resource> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val importDirectives = PsiProvider.extractImportDirectiveFromProject(project)
        val results = importDirectives.map { ImportDirectivePsiAnalyzer.analyze(it) }
        dependenciesDataWriter.writer.println(results.joinToString(separator = "\n"))
    }
}
