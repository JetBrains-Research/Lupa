package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages

data class InternalUsagesAnalysisResult(
    val declarationFqName: String,
    val usageFqName: String,
    val moduleName: String?,
    val sourceSet: String?
)
