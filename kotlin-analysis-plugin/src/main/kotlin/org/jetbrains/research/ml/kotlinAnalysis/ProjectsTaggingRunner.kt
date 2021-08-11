package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.tagging.ProjectsTaggingExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for tagging project to label projects in dataset with [tags][ProjectTag]. */
object ProjectsTaggingRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-project-tags-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ProjectsTaggingExecutor(args.outputDir).execute(args.inputDir)
    }
}
