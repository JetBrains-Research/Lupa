package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object KotlinAnalysisRunner : ApplicationStarter {
    private lateinit var inputDir: Path
    private lateinit var outputDir: Path

    private val logger = Logger.getInstance(javaClass)

    override fun getCommandName(): String = "kotlin-analysis"

    class PluginRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i",
            "--input",
            help = "Input directory with kotlin projects"
        ) { Paths.get(this) }

        val output by parser.storing(
            "-o",
            "--output",
            help = "Output directory"
        ) { Paths.get(this) }
    }

    override fun main(args: List<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(KotlinAnalysisRunner::PluginRunnerArgs).run {
                inputDir = input
                outputDir = output
            }
            require(inputDir.toFile().isDirectory) { "Argument has to be directory" }
            outputDir.toFile().mkdirs()

            val pipeline = Pipeline(outputDir)
            pipeline.extractMethodsToCloneDetectionFormat(inputDir)
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
