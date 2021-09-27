package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.extensions.getQName
import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for [from import statement][PyFromImportStatement]. Analysis consists of fully qualified
 * name extraction.
 */
object FromImportStatementPsiAnalyzer : PsiAnalyzer<PyFromImportStatement, List<String>> {

    private const val RELATIVE_LEVEL_OF_ABSOLUTE_IMPORT = 0

    /** Get fully qualified name of given [from import statement][PyFromImportStatement]. */
    override fun analyze(psiElement: PyFromImportStatement): List<String> {
        return when (psiElement.relativeLevel) {
            // Processing absolute import
            RELATIVE_LEVEL_OF_ABSOLUTE_IMPORT -> {
                if (psiElement.isStarImport) {
                    psiElement.importSourceQName?.toString()?.let { listOf(it) } ?: emptyList()
                } else {
                    psiElement.fullyQualifiedObjectNames
                }
            }
            // Processing relative import
            else -> {
                val importSourceQName = psiElement.resolveImportSource()?.getQName()?.toString()

                if (psiElement.isStarImport) {
                    importSourceQName?.let { listOf(it) } ?: emptyList()
                } else {
                    val importElements = psiElement.importElements.mapNotNull { it.importedQName?.toString() }
                    importSourceQName?.let { importSource ->
                        importElements.map { importElement -> "$importSource.$importElement" }
                    } ?: emptyList()
                }
            }
        }
    }
}
