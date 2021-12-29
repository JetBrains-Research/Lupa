package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.impl.PyDecoratorImpl

/**
 * Analyzer for [decorator][PyDecoratorImpl].
 * Analysis consists of fully qualified name extraction.
 */
object PyDecoratorAnalyzer : CallExpressionAnalyzer<PyDecoratorImpl>(PyDecoratorImpl::class.java)
