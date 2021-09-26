package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.extensions.getQName
import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for [from import statement][PyFromImportStatement]. Analysis consists of fully qualified
 * name extraction.
 */
object FromImportStatementPsiAnalyzer : PsiAnalyzer<PyFromImportStatement, List<String>> {

    /** Get fully qualified name of given [from import statement][PyFromImportStatement]. */
    override fun analyze(psiElement: PyFromImportStatement): List<String> {
        return when (psiElement.relativeLevel) {
            // Processing absolute import
            0 -> {
                when (psiElement.isStarImport) {
                    true -> psiElement.importSourceQName?.toString()?.let { listOf(it) }
                            ?: emptyList()
                    false -> psiElement.fullyQualifiedObjectNames
                }
            }
            // Processing relative import
            else -> {
                val importSourceQName =
                    psiElement.resolveImportSource()?.getQName()?.toString()
                        ?: psiElement.importSourceQName?.toString()

                when (psiElement.isStarImport) {
                    true -> importSourceQName?.let { listOf(it) } ?: emptyList()
                    false -> {
                        val importElements =
                            psiElement.importElements.mapNotNull { it.importedQName?.toString() }
                        importElements.map { importElement -> "$importSourceQName.$importElement" }
                    }
                }
            }
        }
    }
}
