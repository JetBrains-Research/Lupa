package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.metrics.ProjectMetricsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin project metrics analysis. */
object KotlinProjectMetricsAnalysisAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-project-metrics-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(ProjectMetricsAnalysisExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
