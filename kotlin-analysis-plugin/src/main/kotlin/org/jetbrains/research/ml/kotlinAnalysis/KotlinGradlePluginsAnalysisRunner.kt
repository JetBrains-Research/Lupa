package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradlePluginsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle plugins analysis. */
object KotlinGradlePluginsAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-plugins-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        GradlePluginsAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
