package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

/**
 * Analyzer for imported classes usage.
 * Analysis consists of fully qualified name extraction of class.
 */
object KtReferenceExpressionPsiAnalyzer : PsiAnalyzer<KtReferenceExpression, String> {


    /** Get fully qualified name of given [object constructor][KtImportDirective]. */
    override fun analyze(psiElement: KtReferenceExpression): String? {
        val bindingContext = psiElement.analyze(BodyResolveMode.FULL)
        return psiElement.getType(bindingContext)?.fqName?.asString()
    }
}
