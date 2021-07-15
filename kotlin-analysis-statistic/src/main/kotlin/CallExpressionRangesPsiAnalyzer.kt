package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtCallExpression

object CallExpressionRangesPsiAnalyzer : PsiAnalyzer<KtCallExpression, RangeType> {

    override fun analyze(psiElement: KtCallExpression): RangeType {
        val simpleName = psiElement.calleeExpression?.text
        if (simpleName == "rangeTo") {
            return RangeType.RANGE_TO
        }
        return RangeType.NOT_RANGE
    }
}
