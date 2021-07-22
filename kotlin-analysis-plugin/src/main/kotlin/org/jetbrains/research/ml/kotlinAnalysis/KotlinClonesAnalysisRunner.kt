package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin clones analysis. */
object KotlinClonesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-clones-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        FormattedMethodMiner(args.outputDir).execute(args.inputDir)
    }
}
