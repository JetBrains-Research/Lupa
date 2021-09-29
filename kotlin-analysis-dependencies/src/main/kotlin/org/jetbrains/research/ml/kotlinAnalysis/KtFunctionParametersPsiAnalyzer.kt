package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Analyzer for imported classes usage.
 * Analysis consists of fully qualified name extraction of class.
 */
object KtFunctionParametersPsiAnalyzer : PsiAnalyzer<KtFunction, List<String>> {

    /** Get fully qualified name of given [object constructor][KtImportDirective]. */
    override fun analyze(psiElement: KtFunction): List<String> {
        return psiElement.valueParameters
            .mapNotNullTo(mutableListOf()) { it.fqName?.asString() }
    }
}
