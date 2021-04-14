package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

object KotlinAnalysisRunner : ApplicationStarter {
    private lateinit var inputDir: File

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "kotlin-analysis"

    class PluginRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i",
            "--input",
            help = "Input directory with kotlin files"
        ) { Paths.get(this) }
    }

    override fun main(args: List<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(KotlinAnalysisRunner::PluginRunnerArgs).run {
                inputDir = input.toFile()
            }
            println(inputDir)
            require(inputDir.isDirectory) { "Argument has to be directory" }
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
