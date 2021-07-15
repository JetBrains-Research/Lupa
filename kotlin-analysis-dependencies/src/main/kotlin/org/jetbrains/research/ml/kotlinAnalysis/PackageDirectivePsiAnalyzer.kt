package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtPackageDirective

/**
 * Analyzer for [package directive][KtPackageDirective].
 * Analysis consists of fully-qualified name extraction.
 */
object PackageDirectivePsiAnalyzer : PsiAnalyzer<KtPackageDirective, String> {

    /** Get fully-qualified name of given [package directive][KtPackageDirective]. */
    override fun analyze(psiElement: KtPackageDirective): String {
        return psiElement.qualifiedName
    }
}
