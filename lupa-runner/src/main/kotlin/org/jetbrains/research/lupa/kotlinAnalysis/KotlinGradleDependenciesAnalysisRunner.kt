package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.GradleDependenciesAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle dependencies analysis. */
object KotlinGradleDependenciesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(GradleDependenciesAnalysisExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
