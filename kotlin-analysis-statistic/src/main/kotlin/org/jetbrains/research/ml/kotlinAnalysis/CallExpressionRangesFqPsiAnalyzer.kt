package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull

/**
 * Analyzer for call expressions.
 * Determines whether an expression is a range. If it is, determines the type of the range.
 * This class uses fq names for analysis, so it works correctly only on projects with dependencies.
 */
object CallExpressionRangesFqPsiAnalyzer : PsiAnalyzer<KtCallExpression, RangeType> {

    override fun analyze(psiElement: KtCallExpression): RangeType {
        val methodFqName = psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull()
            ?: throw error("Can't resolve the name of the expression: project opened incorrectly")
        val regex = Regex(RangeType.RANGE_TO.fqName!!)
        if (regex.matches(methodFqName.toString())) {
            return RangeType.RANGE_TO
        }
        return RangeType.NOT_RANGE
    }
}
