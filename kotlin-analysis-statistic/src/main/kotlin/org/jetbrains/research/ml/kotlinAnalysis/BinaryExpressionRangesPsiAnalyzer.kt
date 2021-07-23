package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtBinaryExpression

/**
 * Analyzer for binary expressions.
 * Determines whether an expression is a range. If it is, determines the type of the range.
 * This class uses simple names instead of fq names, so it might count extra cases.
 */
object BinaryExpressionRangesPsiAnalyzer : PsiAnalyzer<KtBinaryExpression, RangeType> {

    override fun analyze(psiElement: KtBinaryExpression): RangeType {
        val operatorName = psiElement.operationReference.getReferencedName()

        if (psiElement.isRangeDots()) {
            return RangeType.DOTS
        } else if (operatorName in arrayOf(RangeType.UNTIL.simpleName, RangeType.DOWN_TO.simpleName)) {
            return RangeType.fromName(operatorName)!!
        }

        return RangeType.NOT_RANGE
    }

    private fun KtBinaryExpression.isRangeDots(): Boolean {
        return this.operationToken.toString() == "RANGE"
    }
}
