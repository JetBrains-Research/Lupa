package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.pythonAnalysis.ImportStatementsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

object PythonDependenciesAnalysisRunner :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("python-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ImportStatementsAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
