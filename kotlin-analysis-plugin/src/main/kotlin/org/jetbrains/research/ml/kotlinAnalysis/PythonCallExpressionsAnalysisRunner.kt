package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.pythonAnalysis.CallExpressionsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner

object PythonCallExpressionsAnalysisRunner :
    BaseRunner<IORunnerArgsWithVenv, IORunnerArgsWithVenvParser>(
        "python-call-expressions-analysis",
        IORunnerArgsWithVenvParser
    ) {
    override fun run(args: IORunnerArgsWithVenv) {
        CallExpressionsAnalysisExecutor(args.outputDir, venv = args.venvDir).execute(args.inputDir)
    }
}
