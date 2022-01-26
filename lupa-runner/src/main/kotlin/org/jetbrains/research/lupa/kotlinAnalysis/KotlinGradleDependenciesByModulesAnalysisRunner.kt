package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.gradle.analysis.gradle.GradleDependenciesByModulesAnalysisExecutor
import org.jetbrains.research.lupa.kotlinAnalysis.util.RepositoryOpenerUtil
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

/** Runner for kotlin gradle dependencies by project's modules analysis. */
object KotlinGradleDependenciesByModulesAnalysisRunner : BaseRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-gradle-dependencies-by-modules-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        GradleDependenciesByModulesAnalysisExecutor(
            args.outputDir,
            repositoryOpener = RepositoryOpenerUtil::openReloadRepositoryOpener
        ).executeAllProjects(args.inputDir)
    }
}
