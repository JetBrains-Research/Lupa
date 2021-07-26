package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import java.nio.file.Path

/**
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportDirectivesAnalysisExecutor(outputDir: Path, filename: String = "import_directives_data.csv") :
    AnalysisExecutor() {

    private val dependenciesDataWriter = PrintWriterResourceManager(outputDir, filename)
    private val projectsDataWriter = PrintWriterResourceManager(outputDir, "not_android_projects_data.csv")
    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter, projectsDataWriter)

    override fun analyse(project: Project) {
        projectsDataWriter.writer.println(project.name.replace('#', '/'))
        projectsDataWriter.writer.flush()
        val packageDirectives = project.extractElementsOfType(KtPackageDirective::class.java)
            .filter { !it.isRoot }
        val projectPackageFqNames = packageDirectives.map { PackageDirectivePsiAnalyzer.analyze(it) }.toSet()
        val importDirectives = project.extractElementsOfType(KtImportDirective::class.java)
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
