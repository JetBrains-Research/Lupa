package org.jetbrains.research.lupa.kotlinAnalysis.dependencies.analysis

import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

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
