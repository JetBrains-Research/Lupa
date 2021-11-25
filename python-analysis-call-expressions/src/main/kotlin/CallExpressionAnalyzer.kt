package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.PyCallExpression
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl

/**
 * Analyzer for [call expression][PyCallExpression].
 * Analysis consists of fully qualified name extraction.
 */
object CallExpressionAnalyzer :
    PsiAnalyzerWithContextImpl<PyCallExpression, CallExpressionAnalyzerContext, String>(PyCallExpression::class.java) {

    /** Get fully qualified name of given [call expression][PyCallExpression]. **/
    override fun analyzeWithContext(psiElement: PyCallExpression, context: CallExpressionAnalyzerContext?): String? {
        return context?.run {
            fqNamesProvider.getQualifiedName(psiElement.multiResolveCalleeFunction(resolveContext).firstOrNull())
        }
    }
}
