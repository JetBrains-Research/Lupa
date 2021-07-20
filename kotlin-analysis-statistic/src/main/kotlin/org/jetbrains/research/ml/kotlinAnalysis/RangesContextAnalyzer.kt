package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

object RangesContextAnalyzer : PsiAnalyzer<PsiElement, ContextType> {

    override fun analyze(psiElement: PsiElement): ContextType {
        var curElement = psiElement.parent
        while (curElement !is KtFile) {
            when {
                curElement is KtForExpression -> {
                    return ContextType.FOR
                }
                curElement is KtWhileExpression -> {
                    return ContextType.WHILE
                }
                curElement is KtProperty -> {
                    return ContextType.PROPERTY
                }
                curElement is KtIfExpression -> {
                    return ContextType.IF
                }
                curElement is KtWhenCondition -> {
                    return ContextType.WHEN
                }
                curElement.isFunctionWithName("forEach") -> {
                    return ContextType.FOREACH
                }
                curElement.isFunctionWithName("map") -> {
                    return ContextType.MAP
                }
                curElement.isFunctionWithName("toList") -> {
                    return ContextType.TOLIST
                }
                curElement is KtCallExpression && curElement.functionName().equals("require") -> {
                    return ContextType.REQUIRE
                }
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
