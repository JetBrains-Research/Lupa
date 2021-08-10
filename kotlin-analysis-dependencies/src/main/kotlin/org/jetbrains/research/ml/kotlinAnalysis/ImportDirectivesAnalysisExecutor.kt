package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import java.nio.file.Path

/**
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportDirectivesAnalysisExecutor(outputDir: Path, filename: String = "import_directives_data.csv") :
    AnalysisExecutor() {

    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val packageDirectives = project.extractElementsOfType(KtPackageDirective::class.java)
            .filter { !it.isRoot }
        val projectPackageFqNames = packageDirectives.map { PackageDirectivePsiAnalyzer.analyze(it) }.toSet()
        val importDirectives = project.extractElementsOfType(KtImportDirective::class.java)
        val results = importDirectives
            .map { ImportDirectivePsiAnalyzer.analyze(it) }
            .filter { importDirective -> !projectPackageFqNames.any { importDirective.startsWith(it) } }
        results.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
        }
    }
}
