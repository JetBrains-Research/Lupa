package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

/**
 * Analyzer for ranges, returns range border identifiers.
 */
object RangesIdentifierLengthAnalyzer : PsiAnalyzer<PsiElement, Pair<String, String>> {

    override fun analyze(psiElement: PsiElement): Pair<String, String>? {
        val left: String
        val right: String
        when (psiElement) {
            is KtBinaryExpression -> {
                left = psiElement.left?.text!!
                right = psiElement.right?.text!!
            }
            is KtCallExpression -> {
                left = (psiElement.parent as KtDotQualifiedExpression).receiverExpression.text
                right = psiElement.valueArguments[0].text
            }
            else -> {
                return null
            }
        }
        return Pair(left, right)
    }
}
