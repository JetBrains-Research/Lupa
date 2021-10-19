package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.extensions.getQName
import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.isAbsoluteImport

/**
 * Analyzer for [from import statement][PyFromImportStatement]. Analysis consists of fully qualified
 * name extraction.
 */
object FromImportStatementPsiAnalyzer : PsiAnalyzer<PyFromImportStatement, List<String>> {
    /** Get fully qualified name of given [from import statement][PyFromImportStatement]. */
    override fun analyze(psiElement: PyFromImportStatement): List<String> {
        return if (psiElement.isAbsoluteImport()) {
            // Processing absolute import
            if (psiElement.isStarImport) {
                psiElement.importSourceQName?.toString()?.let { listOf(it) } ?: emptyList()
            } else {
                psiElement.fullyQualifiedObjectNames
            }
        } else {
            // Processing relative import
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

    /** Get fully qualified name of given [from import statement][PyFromImportStatement].
     * If [ignoreRelativeImports] is enabled, relative imports will be ignored. */
    fun analyze(psiElement: PyFromImportStatement, ignoreRelativeImports: Boolean): List<String> {
        if (ignoreRelativeImports && !psiElement.isAbsoluteImport()) {
            return emptyList()
        }

        return analyze(psiElement)
    }
}
