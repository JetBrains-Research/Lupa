package org.jetbrains.research.ml.kotlinAnalysis

/** Runner for kotlin dependencies analysis. */
object KotlinDependenciesAnalysisRunner : KotlinAnalysisRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-dependencies-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        ImportDirectivesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
