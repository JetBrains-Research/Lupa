package org.jetbrains.research.lupa.pythonAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.AnalysisOrchestrator
import org.jetbrains.research.lupa.pythonAnalysis.imports.analysis.ImportStatementsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

object PythonImportsAnalysisRunner :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("python-imports-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(ImportStatementsAnalysisExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
