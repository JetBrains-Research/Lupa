package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin ranges analysis. */
object KotlinRangesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-ranges-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        RangesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
