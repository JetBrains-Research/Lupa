package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.util.getPrintWriter
import java.io.PrintWriter
import java.nio.file.Path

/** Interface for any resource, which must been initialized before and closed after use. **/
interface ResourceManager : AutoCloseable {
    fun init()
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

    override fun init() {
        writer = getPrintWriter(directory, fileName)
        header?.let { writer.println(header) }
    }

    override fun close() {
        writer.close()
    }
}
