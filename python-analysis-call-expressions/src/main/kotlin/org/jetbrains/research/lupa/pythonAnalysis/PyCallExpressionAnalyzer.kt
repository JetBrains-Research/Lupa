package org.jetbrains.research.lupa.pythonAnalysis

import com.jetbrains.python.psi.impl.PyCallExpressionImpl

/**
 * Analyzer for [call expression][PyCallExpressionImpl].
 * Analysis consists of fully qualified name extraction.
 */
object PyCallExpressionAnalyzer : CallExpressionAnalyzer<PyCallExpressionImpl>(PyCallExpressionImpl::class.java)
