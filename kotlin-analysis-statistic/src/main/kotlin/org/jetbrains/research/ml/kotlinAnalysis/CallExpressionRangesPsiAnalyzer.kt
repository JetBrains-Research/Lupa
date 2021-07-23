package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Analyzer for call expressions.
 * Determines whether an expression is a range. If it is, determines the type of the range.
 * This class uses simple names instead of fq names, so it might count extra cases.
 */
object CallExpressionRangesPsiAnalyzer : PsiAnalyzer<KtCallExpression, RangeType> {

    override fun analyze(psiElement: KtCallExpression): RangeType {
        val simpleName = psiElement.calleeExpression?.text
        if (simpleName == "rangeTo") {
            return RangeType.RANGE_TO
        }
        return RangeType.NOT_RANGE
    }
}
