package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull

/**
 * Analyzer for binary expressions.
 * Determines whether an expression is a range. If it is, determines the type of the range.
 * This class uses fq names for analysis, so it works correctly only on projects with dependencies.
 */
object BinaryExpressionRangesFqPsiAnalyzer : PsiAnalyzer<KtBinaryExpression, RangeType> {
    override fun analyze(psiElement: KtBinaryExpression): RangeType {
        val expressionFqName = psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull()
            ?: throw error("Can't resolve the name of the expression: project opened incorrectly")
        val expressionFqNameStr = expressionFqName.toString()

        // 1..5 case
        val regex = Regex(RangeType.DOTS.fqName!!)
        if (regex.matches(expressionFqNameStr)) {
            return RangeType.DOTS
        }

        // downTo and until cases
        if (expressionFqNameStr == RangeType.UNTIL.fqName || expressionFqNameStr == RangeType.DOWN_TO.fqName) {
            return RangeType.fromFqName(expressionFqNameStr)!!
        }

        return RangeType.NOT_RANGE
    }
}
