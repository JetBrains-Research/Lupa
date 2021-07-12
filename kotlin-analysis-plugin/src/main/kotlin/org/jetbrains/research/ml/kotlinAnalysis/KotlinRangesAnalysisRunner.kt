package org.jetbrains.research.ml.kotlinAnalysis

/** Runner for kotlin ranges analysis. */
object KotlinRangesAnalysisRunner : KotlinAnalysisRunner<IORunnerArgs, IORunnerArgsParser>
    ("kotlin-ranges-analysis", IORunnerArgsParser) {
    override fun run(args: IORunnerArgs) {
        RangesAnalysisExecutor(args.outputDir).execute(args.inputDir)
    }
}
