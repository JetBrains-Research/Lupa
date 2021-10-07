package org.jetbrains.research.ml.kotlinAnalysis.reflection

import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

/**
 * Analyzer for kt name reference expression.
 * Determines whether an expression is a java reflection call.
 * If it is, determines the type of the reflection function.
 * This class uses simple names instead of fq names, so it might count extra cases.
 */
object ReferenceExpressionReflectionPsiAnalyzer : PsiAnalyzer<KtNameReferenceExpression, JavaReflectionFunction> {

    override fun analyze(psiElement: KtNameReferenceExpression) =
        JavaReflectionFunction.fromName(psiElement.getReferencedName()) ?: JavaReflectionFunction.NOT_REFLECTION
}
