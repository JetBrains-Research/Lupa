package org.jetbrains.research.ml.kotlinAnalysis.metrics

import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

class ProjectMetricsAnalyzer: PsiAnalyzer<Project, ProjectMetrics> {
    override fun analyze(psiElement: Project): ProjectMetrics? {
        TODO("Not yet implemented")
    }
}
