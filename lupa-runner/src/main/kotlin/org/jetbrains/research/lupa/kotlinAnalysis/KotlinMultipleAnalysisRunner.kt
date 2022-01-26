package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.GradleDependenciesAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.statistic.analysis.metrics.ProjectMetricsAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.util.kotlin.KotlinTeamDatabaseConnection
import org.jetbrains.research.lupa.kotlinAnalysis.util.kotlin.KotlinTeamExecutorHelper
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for execution multiple kotlin analyzers. */
object KotlinMultipleAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-multiple-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        val dbConn = KotlinTeamDatabaseConnection()
        val kotlinExecutorHelper = KotlinTeamExecutorHelper(dbConn)
        val executors = listOf(GradleDependenciesAnalysisExecutor(args.outputDir),
            ProjectMetricsAnalysisExecutor(args.outputDir))
        MultipleAnalysisOrchestrator(executors, kotlinExecutorHelper).execute(args.inputDir, args.outputDir)
    }
}
