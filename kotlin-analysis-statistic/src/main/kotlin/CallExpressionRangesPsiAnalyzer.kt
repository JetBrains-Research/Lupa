package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull


object CallExpressionRangesPsiAnalyzer : PsiAnalyzer<KtCallExpression, Boolean> {

    override fun analyze(psiElement: KtCallExpression): Boolean {
        val methodFqName = psiElement.resolveToCall()?.resultingDescriptor?.fqNameOrNull().toString()
        var isRange = false
        val regex = Regex("kotlin.*.rangeTo")
        if (regex.matches(methodFqName)) {
            isRange = true
        }
        println(psiElement.text + " " + methodFqName)
        return isRange
    }
}
