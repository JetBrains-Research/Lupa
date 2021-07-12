package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import kotlin.test.assertEquals

object BinaryExpressionRangesPsiAnalyzer : PsiAnalyzer<KtBinaryExpression, RangeType> {

    override fun analyze(psiElement: KtBinaryExpression): RangeType {
        println(psiElement.text + " " + psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull().toString())
        val operatorName = psiElement.operationReference.getReferencedName()
        return when {
            psiElement.isRangeDots() -> {
//                assertEquals((psiElement.operationToken as KtSingleValueToken).value, "..")
                RangeType.DOTS
            }
            operatorName in arrayOf(RangeType.UNTIL.value, RangeType.DOWN_TO.value, RangeType.RANGE_TO.value) -> {
                RangeType.fromString(operatorName)!!
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

