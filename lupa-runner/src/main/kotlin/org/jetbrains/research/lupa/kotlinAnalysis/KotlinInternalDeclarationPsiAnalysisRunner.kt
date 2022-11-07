package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.declarations.InternalDeclarationPsiAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.executor.InternalUsagesPsiAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.executor.InternalUsagesWithResolvePsiAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

object KotlinInternalDeclarationPsiAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-internal-declaration-psi-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        MultipleAnalysisOrchestrator(
            listOf(
                InternalDeclarationPsiAnalysisExecutor(args.outputDir),
                InternalUsagesPsiAnalysisExecutor(args.outputDir),
                InternalUsagesWithResolvePsiAnalysisExecutor(args.outputDir)
            ),
        ).execute(args.inputDir, args.outputDir)
    }
}
