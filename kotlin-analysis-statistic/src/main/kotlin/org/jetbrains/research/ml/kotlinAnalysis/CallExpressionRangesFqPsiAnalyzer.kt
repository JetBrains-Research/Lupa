package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull


object CallExpressionRangesFqPsiAnalyzer : PsiAnalyzer<KtCallExpression, RangeType> {

    override fun analyze(psiElement: KtCallExpression): RangeType {
        val methodFqName = psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull().toString()
        println(psiElement.text + (" ".repeat(40 - psiElement.text.length)) + methodFqName)
        val regex = Regex(RangeType.RANGE_TO.fqName!!)
        if (regex.matches(methodFqName)) {
            return RangeType.RANGE_TO
        }
        return RangeType.NOT_RANGE
    }
}
