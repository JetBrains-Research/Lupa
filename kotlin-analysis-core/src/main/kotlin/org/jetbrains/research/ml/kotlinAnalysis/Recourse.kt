package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.research.ml.kotlinAnalysis.util.getPrintWriter
import java.io.PrintWriter
import java.nio.file.Path

/** Interface for any recourse, which must been initialized before and closed after use. **/
interface Resource : AutoCloseable {
    fun init()
}

/**
 * Recourse wrap for [file writer][PrintWriter]. For given [directory][directory] and [file name][fileName]
 * it gets new instance of [print writer][PrintWriter] in [init][init] method and [close][close] it in close method.
 **/
class PrintWriterRecourse(private val directory: Path, private val fileName: String) : Resource {
    lateinit var writer: PrintWriter

    override fun init() {
        writer = getPrintWriter(directory, fileName)
    }

    override fun close() {
        writer.close()
    }
}
