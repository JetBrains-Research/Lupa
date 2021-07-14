package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
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
        val packageDirectives = PsiProvider.extractElementsOfTypeFromProject(project, KtPackageDirective::class.java)
            .filter { !it.isRoot }
        val projectPackageFqNames = packageDirectives.map { PackageDirectivePsiAnalyzer.analyze(it) }.toSet()
        val importDirectives = PsiProvider.extractElementsOfTypeFromProject(project, KtImportDirective::class.java)
        val results = importDirectives
            .map { ImportDirectivePsiAnalyzer.analyze(it) }
            .filter { importDirective -> !projectPackageFqNames.any { importDirective.startsWith(it) } }
        if (results.isNotEmpty()) {
            dependenciesDataWriter.writer.println(
                results.joinToString(separator = System.getProperty("line.separator"))
            )
        }
    }
}
