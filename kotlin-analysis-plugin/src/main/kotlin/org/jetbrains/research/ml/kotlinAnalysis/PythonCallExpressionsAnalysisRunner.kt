package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.pythonAnalysis.CallExpressionsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

object PythonCallExpressionsAnalysisRunner :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("python-functions-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        CallExpressionsAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
