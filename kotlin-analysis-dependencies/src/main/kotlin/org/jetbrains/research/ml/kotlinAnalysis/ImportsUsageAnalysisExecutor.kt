package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import java.nio.file.Path

/**
 * Executor for import directives analysis which collects full qualified names of all import directives in projects
 * and stores them to file in [output directory][outputDir].
 */
class ImportsUsageAnalysisExecutor(outputDir: Path, filename: String = "imports_usage_data.csv") :
    AnalysisExecutor() {

    private val importsUsageDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "fq_name", "type").joinToString(separator = ",")
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(importsUsageDataWriter)


    override fun analyse(project: Project) {
        val packageDirectives = project.extractElementsOfType(KtPackageDirective::class.java)
            .filter { !it.isRoot }
        val projectPackageFqNames = packageDirectives.map { PackageDirectivePsiAnalyzer.analyze(it) }.toSet()

        val callExpressions = project.extractElementsOfType(KtCallExpression::class.java)

        callExpressions
            .mapNotNull { KtCallExpressionPsiAnalyzer.analyze(it) }
            .filter { importDirective -> !projectPackageFqNames.any { importDirective.startsWith(it) } }
            .ifNotEmpty {
                importsUsageDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                    listOf(project.name, it, "call_expression").joinToString(separator = ",")
                })
            }

        val functions = project.extractElementsOfType(KtFunction::class.java)

        functions
            .flatMap { KtFunctionParametersPsiAnalyzer.analyze(it) }
            .filter { importDirective -> !projectPackageFqNames.any { importDirective.startsWith(it) } }
            .ifNotEmpty {
                importsUsageDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                    listOf(project.name, it, "function_parameter").joinToString(separator = ",")
                })
            }

        val references = project.extractElementsOfType(KtReferenceExpression::class.java)

        references
            .mapNotNull { KtReferenceExpressionPsiAnalyzer.analyze(it) }
            .filter { importDirective ->
                projectPackageFqNames.isNotEmpty() and !projectPackageFqNames.any {
                    importDirective.startsWith(
                        it
                    )
                }
            }
            .ifNotEmpty {
                importsUsageDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                    listOf(project.name, it, "reference").joinToString(separator = ",")
                })
            }
    }
}
