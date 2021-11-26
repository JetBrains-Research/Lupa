package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.impl.PyCallExpressionImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl

/**
 * Analyzer for [call expression][PyCallExpressionImpl].
 * Analysis consists of fully qualified name extraction.
 */
object CallExpressionAnalyzer :
    PsiAnalyzerWithContextImpl<PyCallExpressionImpl, CallExpressionAnalyzerContext, String>(PyCallExpressionImpl::class.java) {

    /** Get fully qualified name of given [call expression][PyCallExpressionImpl]. **/
    override fun analyzeWithContext(
        psiElement: PyCallExpressionImpl,
        context: CallExpressionAnalyzerContext?
    ): String? {
        return context?.run {
            fqNamesProvider.getQualifiedName(psiElement.multiResolveCalleeFunction(resolveContext).firstOrNull())
        }
    }
}
