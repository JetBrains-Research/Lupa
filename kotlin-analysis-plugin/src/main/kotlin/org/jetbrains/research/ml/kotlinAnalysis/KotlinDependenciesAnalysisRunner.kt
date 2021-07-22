package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin dependencies analysis. */
object KotlinDependenciesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ImportDirectivesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
