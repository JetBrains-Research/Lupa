package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.resolve.PyResolveContext
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerContext

/**
 * Context for [CallExpressionAnalyzer].
 * Contains [resolveContext][PyResolveContext] and [fqNamesProvider][PyQualifiedNameProvider].
 */
data class CallExpressionAnalyzerContext(
    val resolveContext: PyResolveContext,
    val fqNamesProvider: PyQualifiedNameProvider,
) : AnalyzerContext
