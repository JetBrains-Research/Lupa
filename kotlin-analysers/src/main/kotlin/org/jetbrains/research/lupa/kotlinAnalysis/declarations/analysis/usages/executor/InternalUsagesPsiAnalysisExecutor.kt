package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.executor

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.ExecutorHelper
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer.InternalUsagesPsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import java.nio.file.Path

class InternalUsagesPsiAnalysisExecutor(
    outputDir: Path,
    executorHelper: ExecutorHelper? = null,
    repositoryOpener: (Path, (Project) -> Boolean) -> Boolean =
        RepositoryOpenerUtil.Companion::openReloadKotlinJavaRepositoryOpener,
    filename: String = "internal_usages.csv"
) : InternalUsagesPsiAnalysisExecutorBase(filename, outputDir, executorHelper, repositoryOpener) {

    override fun analyse(project: Project) {
        analyse(project, KtNamedDeclaration::class.java) {
            InternalUsagesPsiAnalyzer.analyze(it as KtNamedDeclaration)
        }
    }
}
