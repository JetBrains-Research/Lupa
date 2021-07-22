package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

/**
 * Analyzer that determines in which context the range is used.
 * For example, a range can be used in for loop or in a conditional statement.
 */
object RangesContextAnalyzer : PsiAnalyzer<PsiElement, ContextType> {

    override fun analyze(psiElement: PsiElement): ContextType {
        var curElement = psiElement.parent
        while (curElement !is KtFile) {
            ContextType.fromElement(curElement)?.let { return it }

            if (curElement.isFunctionWithName("forEach")) {
                return ContextType.FOREACH
            } else if (curElement.isFunctionWithName("map")) {
                return ContextType.MAP
            } else if (curElement.isFunctionWithName("toList")) {
                return ContextType.TOLIST
            } else if (curElement is KtCallExpression && curElement.functionName().equals("require")) {
                return ContextType.REQUIRE
            }
            curElement = curElement.parent
        }
        return ContextType.OTHER
    }

    private fun KtCallExpression.functionName(): String? {
        return this.calleeExpression?.text
    }

    private fun PsiElement.isFunctionWithName(functionName: String): Boolean {
        return this is KtDotQualifiedExpression && this.selectorExpression is KtCallExpression &&
                (this.selectorExpression as KtCallExpression).functionName().equals(functionName)
    }
}
