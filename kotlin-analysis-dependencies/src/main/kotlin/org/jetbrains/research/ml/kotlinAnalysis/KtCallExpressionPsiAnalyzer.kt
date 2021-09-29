package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull

/**
 * Analyzer for imported classes usage.
 * Analysis consists of fully qualified name extraction of class.
 */
object KtCallExpressionPsiAnalyzer : PsiAnalyzer<KtCallExpression, String> {

    /** Get fully qualified name of given [object constructor][KtImportDirective]. */
    override fun analyze(psiElement: KtCallExpression): String? {
        return psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull()?.toString()
    }
}
