package org.jetbrains.research.lupa.pythonAnalysis

import org.jetbrains.research.lupa.IORunnerArgsWithVenv
import org.jetbrains.research.lupa.IORunnerArgsWithVenvParser
import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisOrchestrator
import org.jetbrains.research.lupa.pythonAnalysis.callExpressions.analysis.CallExpressionsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner

object PythonCallExpressionsAnalysisRunner :
    BaseRunner<IORunnerArgsWithVenv, IORunnerArgsWithVenvParser>(
        "python-call-expressions-analysis",
        IORunnerArgsWithVenvParser
    ) {
    override fun run(args: IORunnerArgsWithVenv) {
        AnalysisOrchestrator(
            CallExpressionsAnalysisExecutor(
                args.outputDir,
                venv = args.venvDir,
            )
        ).execute(args.inputDir, args.outputDir)
    }
}
