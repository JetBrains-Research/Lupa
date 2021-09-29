package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin dependencies analysis. */
object KotlinImportsUsageAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-imports-usage-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ImportsUsageAnalysisExecutor(args.outputDir)
            .execute(args.inputDir, RepositoryOpenerUtil::openReloadRepositoryOpener)
    }
}
