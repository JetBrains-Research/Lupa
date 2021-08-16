package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.gradle.GradleDependenciesByModulesAnalysisExecutor
import org.jetbrains.research.ml.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle dependencies by project's modules analysis. */
object KotlinGradleDependenciesByModulesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-dependencies-by-modules-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        GradleDependenciesByModulesAnalysisExecutor(args.outputDir)
            .execute(args.inputDir, RepositoryOpenerUtil::openReloadRepositoryOpener)
    }
}
