package org.jetbrains.research.ml.pythonAnalysis

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportStatement
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import java.nio.file.Path

/**
 * Executor for import statements analysis which collects full qualified names of all import statements in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportStatementsAnalysisExecutor(outputDir: Path, filename: String = "import_statements_data.csv") :
    AnalysisExecutor() {
    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val importStatements =
            project.extractPyElementsOfType(PyImportStatement::class.java)

        val fqNames = importStatements.map { ImportStatementPsiAnalyzer.analyze(it) }.flatten().toMutableSet()

        val fromImportStatements =
            project.extractPyElementsOfType(PyFromImportStatement::class.java)

        fqNames.addAll(
            fromImportStatements.map { FromImportStatementPsiAnalyzer.analyze(it) }.flatten()
        )

        fqNames.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
        }
    }
}
