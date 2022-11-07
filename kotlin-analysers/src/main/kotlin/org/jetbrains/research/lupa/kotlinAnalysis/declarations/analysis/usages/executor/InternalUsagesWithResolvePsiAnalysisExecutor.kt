package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.executor

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer.InternalUsagesWithResolvePsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

class InternalUsagesWithResolvePsiAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
    filename: String = "internal_usages_with_resolve.csv"
) : InternalUsagesPsiAnalysisExecutorBase(filename, outputDir, executorHelper, repositoryOpener) {

    override fun analyse(project: Project) {
        analyse(project, KtNameReferenceExpression::class.java) {
            InternalUsagesWithResolvePsiAnalyzer.analyze(it as KtNameReferenceExpression)
        }
    }
}
