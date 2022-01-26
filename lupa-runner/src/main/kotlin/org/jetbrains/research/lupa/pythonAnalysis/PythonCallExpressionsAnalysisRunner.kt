package org.jetbrains.research.lupa.pythonAnalysis

import org.jetbrains.research.lupa.IORunnerArgsWithVenv
import org.jetbrains.research.lupa.IORunnerArgsWithVenvParser
import org.jetbrains.research.lupa.pythonAnalysis.callExpressions.analysis.CallExpressionsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner

object PythonCallExpressionsAnalysisRunner :
    BaseRunner<IORunnerArgsWithVenv, IORunnerArgsWithVenvParser>(
        "python-call-expressions-analysis",
        IORunnerArgsWithVenvParser
    ) {
    override fun run(args: IORunnerArgsWithVenv) {
        CallExpressionsAnalysisExecutor(args.outputDir, venv = args.venvDir).executeAllProjects(args.inputDir)
    }
}
