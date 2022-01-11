package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.GradlePropertiesAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle properties analysis. */
object KotlinGradlePropertiesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-properties-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        GradlePropertiesAnalysisExecutor(args.outputDir).execute(
            args.inputDir,
            repositoryOpener = RepositoryOpenerUtil.Companion::standardRepositoryOpener
        )
    }
}
