package org.jetbrains.research.lupa.pythonAnalysis

import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.resolve.PyResolveContext
import org.jetbrains.research.lupa.kotlinAnalysis.AnalyzerContext

/**
 * Context for [CallExpressionAnalyzer].
 * Contains [resolveContext][PyResolveContext] and [fqNamesProvider][PyQualifiedNameProvider].
 */
data class CallExpressionAnalyzerContext(
    val resolveContext: PyResolveContext,
    val fqNamesProvider: PyQualifiedNameProvider,
) : AnalyzerContext
