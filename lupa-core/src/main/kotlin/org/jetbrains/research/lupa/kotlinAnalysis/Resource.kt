package org.jetbrains.research.lupa.kotlinAnalysis

import org.jetbrains.research.lupa.kotlinAnalysis.util.getPrintWriter
import java.io.PrintWriter
import java.nio.file.Path

/** Interface for any resource, which must been initialized before and closed after use. **/
interface ResourceManager : AutoCloseable {
    fun init(relativePath: Path? = null)
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

    override fun init(relativePath: Path?) {
        val directoryPath = relativePath?.let { directory.resolve(relativePath) } ?: directory
        writer = getPrintWriter(directoryPath, fileName)
        header?.let { writer.println(header) }
    }

    override fun close() {
        writer.close()
    }
}
