package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

enum class Extension(val ext: String) {
    KT("kt"),
    KTS("kts")
}

fun VirtualFile.isKotlinRelatedFile(): Boolean {
    return this.extension == Extension.KT.ext || this.extension == Extension.KTS.ext
}

fun getSubdirectories(path: Path): List<Path> {
    return Files.walk(path, 1)
        .filter { Files.isDirectory(it) && !it.equals(path) }
        .toList()
}

fun printWriter(directory: Path, fileName: String): PrintWriter {
    return File(directory.toFile(), fileName).printWriter()
}
