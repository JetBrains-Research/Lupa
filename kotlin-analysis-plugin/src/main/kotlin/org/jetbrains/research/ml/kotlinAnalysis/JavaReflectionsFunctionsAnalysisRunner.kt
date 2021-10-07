package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.reflection.ReflectionAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin ranges analysis. */
object JavaReflectionsFunctionsAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("java-reflections-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ReflectionAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
