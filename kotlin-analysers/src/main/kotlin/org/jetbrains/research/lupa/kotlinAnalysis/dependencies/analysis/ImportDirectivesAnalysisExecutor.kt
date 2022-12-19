package org.jetbrains.research.lupa.kotlinAnalysis.dependencies.analysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
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
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportDirectivesAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "import_directives_data.csv"
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {

    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = KOTLIN_EXTENSIONS

    override fun analyse(project: Project) {
        val packageDirectives = ApplicationManager.getApplication().runReadAction<List<KtPackageDirective>> {
            project.extractKtElementsOfType(KtPackageDirective::class.java)
                .filter { !it.isRoot }
        }
        val projectPackageFqNames = packageDirectives.map {
            ApplicationManager.getApplication().runReadAction<String> {
                PackageDirectivePsiAnalyzer.analyze(it)
            }
        }.toSet()
        val importDirectives = ApplicationManager.getApplication().runReadAction<List<KtImportDirective>> {
            project.extractKtElementsOfType(KtImportDirective::class.java)
        }
        val results = importDirectives
            .map {
                ApplicationManager.getApplication().runReadAction<String> {
                    ImportDirectivePsiAnalyzer.analyze(it)
                }
            }
            .filter { importDirective -> !projectPackageFqNames.any { importDirective.startsWith(it) } }
        results.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
        }
    }
}
