package org.jetbrains.research.ml.kotlinAnalysis

/** Runner for kotlin clones analysis. */
object KotlinClonesAnalysisRunner : KotlinAnalysisRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-clones-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        FormattedMethodMiner(args.outputDir).execute(args.inputDir)
    }
}
