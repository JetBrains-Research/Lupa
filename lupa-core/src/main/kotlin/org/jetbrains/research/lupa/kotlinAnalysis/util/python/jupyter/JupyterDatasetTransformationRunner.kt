package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

object JupyterDatasetTransformationRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("preprocessJupyterDataset", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        println("IM HERE")
        val transformer = JupyterDatasetTransformer(args.inputDir, args.outputDir)
        transformer.transformDataset()
    }
}
