package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.metrics.ProjectMetricsAnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin project metrics analysis. */
object KotlinProjectMetricsAnalysisAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-project-metrics-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ProjectMetricsAnalysisExecutor(args.outputDir)
            .execute(args.inputDir, RepositoryOpenerUtil::openReloadRepositoryOpener)
    }
}
