package org.jetbrains.research.lupa.pythonAnalysis.imports.analysis

import com.jetbrains.python.psi.PyImportStatement
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for [import statement][PyImportStatement].
 * Analysis consists of fully qualified name extraction.
 */
object ImportStatementPsiAnalyzer : PsiAnalyzer<PyImportStatement, List<String>> {

    /** Get fully qualified name of given [import statement][PyImportStatement]. */
    override fun analyze(psiElement: PyImportStatement): List<String> {
        return psiElement.fullyQualifiedObjectNames
    }
}
