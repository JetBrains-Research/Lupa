package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.util.getPrintWriter
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths

/** Interface for any resource, which must been initialized before and closed after use. **/
interface ResourceManager : AutoCloseable {
    fun init(relativePath: String? = null)
}

/**
 * Resource manager for [file writer][PrintWriter]. For given [directory][directory] and [file name][fileName]
 * it gets new instance of [print writer][PrintWriter] in [init][init] method and [close][close] it in close method.
 **/
class PrintWriterResourceManager(
    private val directory: Path,
    private val fileName: String,
    private val header: String? = null
) : ResourceManager {
    lateinit var writer: PrintWriter

    override fun init(relativePath: String?) {
        val directoryPath: Path = if (relativePath != null) {
            Paths.get(directory.toString(), relativePath)
        } else {
            directory
        }

        writer = getPrintWriter(directoryPath, fileName)
        header?.let { writer.println(header) }
    }

    override fun close() {
        writer.close()
    }
}
