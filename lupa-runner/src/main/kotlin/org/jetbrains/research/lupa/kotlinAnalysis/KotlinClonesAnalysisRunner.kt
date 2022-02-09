package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.clones.analysis.FormattedMethodMiner
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin clones analysis. */
object KotlinClonesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-clones-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        FormattedMethodMiner(args.outputDir).executeAllProjects(args.inputDir)
    }
}
