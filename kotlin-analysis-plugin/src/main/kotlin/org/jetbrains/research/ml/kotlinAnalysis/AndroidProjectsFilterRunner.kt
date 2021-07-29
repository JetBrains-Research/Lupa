package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for android projects filter to create dataset without android projects. */
object AndroidProjectsFilterRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-android-filter", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AndroidProjectsFilterExecutor(args.outputDir).execute(args.inputDir)
    }
}
