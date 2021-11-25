package org.jetbrains.research.ml.pythonAnalysis

import com.intellij.openapi.project.Project
import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.PyFunctionType
import com.jetbrains.python.psi.types.PyUnionType
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import org.jetbrains.research.ml.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.ResourceManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import org.jetbrains.research.ml.kotlinAnalysis.util.python.PyPackageUtil
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject
import java.nio.file.Path

/**
 * Executor for call expressions analysis which collects full qualified names of all call expressions in projects
 * and stores them to file in [output directory][outputDir].
 */
class CallExpressionsAnalysisExecutor(
    outputDir: Path, filename: String = "call_expressions_data.csv", private val venv: Path?,
) : AnalysisExecutor() {
    private val dependenciesDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "fq_name", "category").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(dependenciesDataWriter)

    override fun analyse(project: Project) {
        venv?.let { setSdkToProject(project, venv.toString()) }

        val typeEvalContext = TypeEvalContext.deepCodeInsight(project)
        val pyResolveContext = PyResolveContext.defaultContext(typeEvalContext)
        val fqNamesProvider = PyQualifiedNameProvider()

        val analyzerContext = CallExpressionAnalyzerContext(pyResolveContext, fqNamesProvider)

        val callExpressions = project.extractPyElementsOfType(PyCallExpression::class.java)
        val packageNames = PyPackageUtil.gatherPackageNames(project)

        val callExpressionsByCategory = callExpressions.groupBy { ExpressionCategory.getCategory(it, typeEvalContext) }

        val fqNamesByCategory = callExpressionsByCategory.mapValues { (_, callExpressions) ->
            callExpressions.mapNotNull { CallExpressionAnalyzer.analyze(it, analyzerContext) }.toMutableSet()
        }

        fqNamesByCategory.forEach { (_, fqNames) ->
            fqNames.removeAll { fqName ->
                PyPackageUtil.isFqNameInAnyPackage(
                    fqName,
                    packageNames
                )
            }
        }

        fqNamesByCategory.forEach { (key, value) ->
            value.ifNotEmpty {
                dependenciesDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                    listOf(project.name, it, key.name.lowercase()).joinToString(separator = ",")
                })
            }
        }
    }

    private enum class ExpressionCategory {
        DECORATOR,
        FUNCTION,
        CLASS,
        UNKNOWN;

        companion object {
            fun getCategory(callExpression: PyCallExpression, context: TypeEvalContext): ExpressionCategory {
                if (callExpression is PyDecorator) {
                    return DECORATOR
                }

                var calleeType = callExpression.callee?.let { context.getType(it) } ?: return UNKNOWN

                if (calleeType is PyUnionType) {
                    calleeType = calleeType.members.firstOrNull() ?: return UNKNOWN
                }

                return when (calleeType) {
                    is PyClassType -> CLASS
                    is PyFunctionType -> FUNCTION
                    else -> UNKNOWN
                }
            }
        }
    }
}
