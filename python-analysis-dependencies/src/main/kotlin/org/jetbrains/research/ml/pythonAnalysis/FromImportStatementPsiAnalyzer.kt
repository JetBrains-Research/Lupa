package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for [from import statement][PyFromImportStatement].
 * Analysis consists of fully qualified name extraction.
 */
object FromImportStatementPsiAnalyzer : PsiAnalyzer<PyFromImportStatement, List<String>> {

    /** Get fully qualified name of given [from import statement][PyFromImportStatement]. */
    override fun analyze(psiElement: PyFromImportStatement): List<String> {
        return if (psiElement.isStarImport) {
            listOf(psiElement.importSourceQName.toString())
        } else {
            psiElement.fullyQualifiedObjectNames
        }
    }
}
