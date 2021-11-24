package org.jetbrains.research.ml.kotlinAnalysis

import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionsAnalysisExecutor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.RunnerArgsParser
import java.nio.file.Paths

// TODO
class IORunnerArgsWithVenv(parser: ArgParser) : IORunnerArgs(parser) {
    val venvDir by parser.storing(
        "--venv",
        help = "The path to the virtual environment"
    ) { Paths.get(this) }
}

// TODO
object IORunnerArgsWithVenvParser : RunnerArgsParser<IORunnerArgsWithVenv> {
    override fun parse(args: List<String>): IORunnerArgsWithVenv {
        return ArgParser(args.drop(1).toTypedArray()).parseInto(::IORunnerArgsWithVenv)
            .run {
                require(inputDir.toFile().isDirectory) { "Argument has to be directory" }
                outputDir.toFile().mkdirs()
                require(venvDir.toFile().isDirectory) { "Argument has to be directory" }
                this
            }
    }
}

object PythonCallExpressionsAnalysisRunner :
    BaseRunner<IORunnerArgsWithVenv, IORunnerArgsWithVenvParser>(
        "python-call-expressions-analysis",
        IORunnerArgsWithVenvParser
    ) {
    override fun run(args: IORunnerArgsWithVenv) {
        CallExpressionsAnalysisExecutor(args.outputDir, venv = args.venvDir).execute(args.inputDir)
    }
}
