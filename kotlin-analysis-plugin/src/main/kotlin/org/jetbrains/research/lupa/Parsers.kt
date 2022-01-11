package org.jetbrains.research.lupa

import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.lupa.kotlinAnalysis.util.requireDirectory
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.RunnerArgsParser
import java.nio.file.Paths

/** Arguments data class for input and output directory names arguments,
 *  and also for the path to the virtual environment.*/
class IORunnerArgsWithVenv(parser: ArgParser) : IORunnerArgs(parser) {
    val venvDir by parser.storing(
        "--venv",
        help = "The path to the virtual environment"
    ) {
        if (this == "") null else Paths.get(this)
    }
}

/** Parser for input and output directory names arguments, and also for the path to the virtual environment. */
object IORunnerArgsWithVenvParser : RunnerArgsParser<IORunnerArgsWithVenv> {
    override fun parse(args: List<String>): IORunnerArgsWithVenv {
        return ArgParser(args.drop(1).toTypedArray()).parseInto(::IORunnerArgsWithVenv)
            .run {
                requireDirectory(inputDir)
                outputDir.toFile().mkdirs()
                venvDir?.let { requireDirectory(it) }
                this
            }
    }
}
