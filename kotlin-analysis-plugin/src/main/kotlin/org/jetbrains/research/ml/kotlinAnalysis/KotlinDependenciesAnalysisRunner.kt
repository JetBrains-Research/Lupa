package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.diagnostic.Logger

/** Runner for kotlin dependencies analysis. */
object KotlinDependenciesAnalysisRunner : KotlinAnalysisRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-dependencies-analysis", IORunnerArgsParser) {

    private val logger: Logger = Logger.getInstance(javaClass)

    override fun run(args: IORunnerArgs) {
        ImportDirectivesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
