package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.impl.PyDecoratorImpl
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzerWithContextImpl

/**
 * Analyzer for [decorator][PyDecoratorImpl].
 * Analysis consists of fully qualified name extraction.
 */
object DecoratorAnalyzer :
    PsiAnalyzerWithContextImpl<PyDecoratorImpl, CallExpressionAnalyzerContext, String>(PyDecoratorImpl::class.java) {

    /** Get fully qualified name of given [decorator][PyDecoratorImpl]. **/
    override fun analyzeWithContext(
        psiElement: PyDecoratorImpl,
        context: CallExpressionAnalyzerContext?
    ): String? {
        return context?.run {
            fqNamesProvider.getQualifiedName(psiElement.multiResolveCalleeFunction(resolveContext).firstOrNull())
        }
    }
}
