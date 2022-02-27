package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.reflection.ReflectionAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin ranges analysis. */
object JavaReflectionsFunctionsAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("java-reflections-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(ReflectionAnalysisExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
