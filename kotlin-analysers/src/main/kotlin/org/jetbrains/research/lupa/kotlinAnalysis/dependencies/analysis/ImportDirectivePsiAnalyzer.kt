package org.jetbrains.research.lupa.kotlinAnalysis.dependencies.analysis

import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for [import directive][KtImportDirective].
 * Analysis consists of fully qualified name extraction.
 */
object ImportDirectivePsiAnalyzer : PsiAnalyzer<KtImportDirective, String> {

    /** Get fully qualified name of given [import directive][KtImportDirective]. */
    override fun analyze(psiElement: KtImportDirective): String {
        return psiElement.importedFqName.toString()
    }
}
