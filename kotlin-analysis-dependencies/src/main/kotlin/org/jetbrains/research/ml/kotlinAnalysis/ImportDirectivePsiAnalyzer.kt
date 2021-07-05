package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtImportDirective

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
