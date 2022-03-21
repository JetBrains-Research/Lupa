package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import kotlin.system.exitProcess

class PreprocessJupyterDatasetCommand : CliktCommand(name = "preprocessJupyterDataset") {
    private val input by option("--input").file(canBeFile = false).required()
    private val output by option("--output").file(canBeFile = false).required()

    override fun run() {
        val transformer = JupyterDatasetTransformer(input.toPath(), output.toPath())
        transformer.transformDataset()
        exitProcess(0)
    }
}

object PreprocessJupyterDatasetStarter : ApplicationStarter {
    override fun getCommandName(): String = "preprocessJupyterDataset"
    override fun main(args: MutableList<String>) = PreprocessJupyterDatasetCommand().main(args.drop(1))
}
