package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.PyCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl

object CallExpressionAnalyzer :
    PsiAnalyzerWithContextImpl<PyCallExpression, CallExpressionAnalyzerContext, String>(PyCallExpression::class.java) {
    override fun analyzeWithContext(psiElement: PyCallExpression, context: CallExpressionAnalyzerContext?): String? {
        return context?.run {
            fqNamesProvider.getQualifiedName(psiElement.multiResolveCalleeFunction(resolveContext).firstOrNull())
        }
    }
}
