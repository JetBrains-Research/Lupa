package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtBinaryExpression

object BinaryExpressionRangesPsiAnalyzer : PsiAnalyzer<KtBinaryExpression, RangeType> {

    override fun analyze(psiElement: KtBinaryExpression): RangeType {
        val operatorName = psiElement.operationReference.getReferencedName()
        return when {
            psiElement.isRangeDots() -> {
//                assertEquals((psiElement.operationToken as KtSingleValueToken).value, "..")
                RangeType.DOTS
            }
            operatorName in arrayOf(RangeType.UNTIL.simpleName, RangeType.DOWN_TO.simpleName) -> {
                RangeType.fromName(operatorName)!!
            }
            else -> {
                RangeType.NOT_RANGE
            }
        }
    }

    private fun KtBinaryExpression.isRangeDots(): Boolean {
        return this.operationToken.toString() == "RANGE"
    }
}

