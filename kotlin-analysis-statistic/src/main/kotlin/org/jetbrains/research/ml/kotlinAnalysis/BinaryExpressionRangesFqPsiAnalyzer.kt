package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull

object BinaryExpressionRangesFqPsiAnalyzer : PsiAnalyzer<KtBinaryExpression, RangeType> {
    override fun analyze(psiElement: KtBinaryExpression): RangeType {
        val expressionFqName = psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull().toString()
        println(psiElement.text + (" ".repeat(40 - psiElement.text.length)) + expressionFqName)

        // 1..5 case
        val regex = Regex(RangeType.DOTS.fqName!!)
        if (regex.matches(expressionFqName)) {
            return RangeType.DOTS
        }

        // downTo and until cases
        if (expressionFqName == RangeType.UNTIL.fqName || expressionFqName == RangeType.DOWN_TO.fqName) {
            return RangeType.fromFqName(expressionFqName)!!
        }

        return RangeType.NOT_RANGE
    }
}
