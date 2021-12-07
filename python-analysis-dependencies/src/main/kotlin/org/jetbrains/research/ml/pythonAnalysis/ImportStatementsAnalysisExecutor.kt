package org.jetbrains.research.ml.pythonAnalysis

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportStatement
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import org.jetbrains.research.ml.kotlinAnalysis.util.python.PyPackageUtil
import java.nio.file.Path

/**
 * Executor for import statements analysis which collects full qualified names of all import statements in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportStatementsAnalysisExecutor(outputDir: Path, filename: String = "import_statements_data.csv") :
    AnalysisExecutor() {
    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "import").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        val importStatements = project.extractPyElementsOfType(PyImportStatement::class.java)
        println("${importStatements.size} import statements were extracted.")

        val fqNames = importStatements.flatMap { ImportStatementPsiAnalyzer.analyze(it) }.toMutableSet()

        val fromImportStatements = project.extractPyElementsOfType(PyFromImportStatement::class.java)
        println("${importStatements.size} from import statements were extracted.")

        fqNames.addAll(
            fromImportStatements.flatMap {
                FromImportStatementPsiAnalyzer.analyze(
                    it,
                    ignoreRelativeImports = true,
                )
            }
        )

        val packageNames = PyPackageUtil.gatherPackageNames(project)
        println("${packageNames.size} package names were gathered.")

        fqNames.removeAll { importName -> PyPackageUtil.isFqNameInAnyPackage(importName, packageNames) }

        fqNames.ifNotEmpty {
            dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                listOf(project.name, it).joinToString(separator = ",")
            })
            println("$size unique full qualified names were collected.")
        }
    }
}
