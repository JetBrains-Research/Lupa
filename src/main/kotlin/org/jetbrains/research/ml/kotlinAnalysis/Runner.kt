package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import java.io.File
import kotlin.system.exitProcess


object Runner : ApplicationStarter {
    private lateinit var inputDir: File

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "kotlin-analysis"

    class PluginRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i",
            "--input_path",
            help = "Input directory with kotlin files"
        ) { File(this) }
    }

    override fun main(args: List<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(Runner::PluginRunnerArgs).run {
                inputDir = input
            }
            println(inputDir)
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
