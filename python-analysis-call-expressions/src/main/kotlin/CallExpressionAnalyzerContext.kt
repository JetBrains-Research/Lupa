package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.resolve.PyResolveContext
import org.jetbrains.research.ml.kotlinAnalysis.AnalyzerContext

data class CallExpressionAnalyzerContext(
    val resolveContext: PyResolveContext,
    val fqNamesProvider: PyQualifiedNameProvider,
) : AnalyzerContext
