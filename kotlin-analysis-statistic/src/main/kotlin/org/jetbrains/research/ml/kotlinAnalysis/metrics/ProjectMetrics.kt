package org.jetbrains.research.ml.kotlinAnalysis.metrics

data class ProjectMetrics(
    val ktModulesMetrics: List<ModuleMetrics>
)

data class ModuleMetrics(
    val ktFilesMetrics: List<FileMetrics>
)

data class FileMetrics(
    val ktCodeLines: Int
)
