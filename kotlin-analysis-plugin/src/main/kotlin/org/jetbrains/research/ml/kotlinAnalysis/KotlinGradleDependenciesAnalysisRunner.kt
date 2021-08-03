package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleDependenciesAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle dependencies analysis. */
object KotlinGradleDependenciesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        GradleDependenciesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
