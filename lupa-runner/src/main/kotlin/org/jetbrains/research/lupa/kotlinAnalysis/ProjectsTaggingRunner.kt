package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.tagging.ProjectsTaggingExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for tagging project to label projects in dataset with [tags][ProjectTag]. */
object ProjectsTaggingRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-project-tags-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        AnalysisOrchestrator(ProjectsTaggingExecutor(args.outputDir)).execute(args.inputDir, args.outputDir)
    }
}
