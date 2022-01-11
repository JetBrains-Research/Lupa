package org.jetbrains.research.lupa.pythonAnalysis

import com.jetbrains.python.psi.PyCallExpression
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzerWithContextImpl

/**
 * Analyzer for call expression with type [P].
 * Analysis consists of fully qualified name extraction.
 *
 * @param P the type of [PyCallExpression] for which we want to perform analysis.
 * @param pClass class of element [P].
 */
open class CallExpressionAnalyzer<P : PyCallExpression>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, CallExpressionAnalyzerContext, String>(pClass) {
    override fun analyzeWithContext(psiElement: P, context: CallExpressionAnalyzerContext?): String? {
        return context?.run {
            fqNamesProvider.getQualifiedName(psiElement.multiResolveCalleeFunction(resolveContext).firstOrNull())
        }
    }
}
