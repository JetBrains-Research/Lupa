package org.jetbrains.research.lupa.pythonAnalysis.callExpressions.analysis

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
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.PrintWriterResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.ResourceManager
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.extractPyElementsOfType
import org.jetbrains.research.lupa.kotlinAnalysis.util.FileExtension
import org.jetbrains.research.lupa.kotlinAnalysis.util.KOTLIN_EXTENSIONS
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.lupa.kotlinAnalysis.util.python.PyPackageUtil
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Executor for call expressions analysis which collects full qualified names of all call expressions in projects
 * and stores them to file in [output directory][outputDir].
 */
class CallExpressionsAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::standardRepositoryOpener,
    filename: String = "call_expressions_data.csv",
    private val venv: Path?,
) : AnalysisExecutor(executorHelper, repositoryOpener) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val expressionsDataWriter = PrintWriterResourceManager(
        outputDir, filename,
        header = listOf("project_name", "fq_name", "category").joinToString(separator = ","),
    )

    override val controlledResourceManagers: Set<ResourceManager> = setOf(expressionsDataWriter)
    override val requiredFileExtensions: Set<FileExtension> = KOTLIN_EXTENSIONS

    override fun analyse(project: Project) {
        venv?.let { setSdkToProject(project, venv.toString()) } ?: logger.warn(
            "The path to the virtual environment has not been passed. The analysis will run without the SDK."
        )

        val typeEvalContext = TypeEvalContext.deepCodeInsight(project)
        val pyResolveContext = PyResolveContext.defaultContext(typeEvalContext)
        val fqNamesProvider = PyQualifiedNameProvider()

        val analyzerContext = CallExpressionAnalyzerContext(pyResolveContext, fqNamesProvider)

        val callExpressions = project.extractPyElementsOfType(PyCallExpression::class.java)
        logger.info("${callExpressions.size} call expressions were extracted.")

        val packageNames = PyPackageUtil.gatherPackageNames(project)
        logger.info("${packageNames.size} package names were gathered.")

        val callExpressionsByCategory = callExpressions.groupBy { ExpressionCategory.getCategory(it, typeEvalContext) }
        callExpressionsByCategory[ExpressionCategory.UNKNOWN]
            ?.let { logger.info("${it.size} call expressions were not categorized.") }

        val fqNamesByCategory = callExpressionsByCategory.mapValues { (_, callExpressions) ->
            callExpressions.mapNotNull {
                when (it) {
                    is PyDecorator -> PyDecoratorAnalyzer.analyze(it, analyzerContext)
                    else -> PyCallExpressionAnalyzer.analyze(it, analyzerContext)
                }
            }.toMutableSet()
        }

        filterLocalFqNames(fqNamesByCategory, packageNames)
        writeFqNames(fqNamesByCategory, project)
    }

    /**
     * Filters local [fully qualified names][fqNamesByCategory] with the obtained local [package names][packageNames].
     */
    private fun filterLocalFqNames(
        fqNamesByCategory: Map<ExpressionCategory, MutableSet<String>>,
        packageNames: Set<String>,
    ) {
        fqNamesByCategory.forEach { (_, fqNames) ->
            fqNames.removeAll { fqName ->
                PyPackageUtil.isFqNameInAnyPackage(
                    fqName,
                    packageNames
                )
            }
        }
    }

    /**
     * Using [data writer][expressionsDataWriter] writes the received
     * [fully qualified names][fqNamesByCategory] into a .csv file.
     */
    private fun writeFqNames(
        fqNamesByCategory: Map<ExpressionCategory, MutableSet<String>>,
        project: Project,
    ) {
        fqNamesByCategory.forEach { (category, fqNames) ->
            fqNames.ifNotEmpty {
                expressionsDataWriter.writer.println(joinToString(separator = System.getProperty("line.separator")) {
                    listOf(project.name, it, category.name.lowercase()).joinToString(separator = ",")
                })
            }
            logger.info("In the $category category were collected ${fqNames.size} unique full qualified names.")
        }
    }

    private enum class ExpressionCategory {
        DECORATOR,
        FUNCTION,
        CLASS,
        UNKNOWN;

        companion object {
            /**
             * Identifies the [call expression][callExpression] category using [context].
             */
            fun getCategory(callExpression: PyCallExpression, context: TypeEvalContext): ExpressionCategory {
                if (callExpression is PyDecorator || callExpression.parent is PyDecorator) {
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
