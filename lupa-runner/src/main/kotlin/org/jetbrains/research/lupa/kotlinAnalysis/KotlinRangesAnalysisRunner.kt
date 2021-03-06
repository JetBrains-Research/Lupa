package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.ranges.RangesAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin ranges analysis. */
object KotlinRangesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-ranges-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(RangesAnalysisExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
