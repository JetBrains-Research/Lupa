package org.jetbrains.research.ml.pythonAnalysis

import com.jetbrains.python.psi.impl.PyCallExpressionImpl

/**
 * Analyzer for [call expression][PyCallExpressionImpl].
 * Analysis consists of fully qualified name extraction.
 */
object PyCallExpressionAnalyzer : CallExpressionAnalyzer<PyCallExpressionImpl>(PyCallExpressionImpl::class.java)
