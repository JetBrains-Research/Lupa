package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.research.pluginUtilities.util.Extension
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun VirtualFile.isKotlinRelatedFile(): Boolean {
    return this.extension == Extension.KT.value || this.extension == Extension.KTS.value
}

fun getSubdirectories(path: Path): List<Path> {
    return Files.walk(path, 1)
        .filter { Files.isDirectory(it) && !it.equals(path) }
        .toList()
}

fun getPrintWriter(directory: Path, fileName: String): PrintWriter {
    return File(directory.toFile(), fileName).printWriter()
}
