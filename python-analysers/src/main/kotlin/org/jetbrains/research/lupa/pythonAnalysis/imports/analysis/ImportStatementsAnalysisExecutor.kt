package org.jetbrains.research.lupa.pythonAnalysis.imports.analysis

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportStatement
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.PYTHON_EXTENSIONS
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.lupa.kotlinAnalysis.util.python.PyPackageUtil
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Executor for import statements analysis which collects full qualified names of all import statements in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportStatementsAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "import_statements_data.csv"
) :
    AnalysisExecutor(executorHelper, repositoryOpener) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = PYTHON_EXTENSIONS

    override fun analyse(project: Project) {
        val importStatements = project.extractPyElementsOfType(PyImportStatement::class.java)
        logger.info("${importStatements.size} import statements were extracted.")

        val fqNames = importStatements.flatMap { ImportStatementPsiAnalyzer.analyze(it) }.toMutableSet()

        val fromImportStatements = project.extractPyElementsOfType(PyFromImportStatement::class.java)
        logger.info("${importStatements.size} from import statements were extracted.")

        fqNames.addAll(
            fromImportStatements.flatMap {
                FromImportStatementPsiAnalyzer.analyze(
                    it,
                    ignoreRelativeImports = true,
                )
            }
        )

        val packageNames = PyPackageUtil.gatherPackageNames(project)
        logger.info("${packageNames.size} package names were gathered.")

        fqNames.removeAll { importName -> PyPackageUtil.isFqNameInAnyPackage(importName, packageNames) }

        fqNames.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
            logger.info("$size unique full qualified names were collected.")
        }
    }
}
