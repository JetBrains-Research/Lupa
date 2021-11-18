package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.inlineFunction.InlineFunctionAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin inline function analysis. */
object KotlinInlineFunctionAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-inline-function-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        InlineFunctionAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
