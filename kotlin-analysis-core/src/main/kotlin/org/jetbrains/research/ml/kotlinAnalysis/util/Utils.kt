package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.psi.KtElement
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

enum class Extension(val value: String) {
    KT("kt"),
    KTS("kts"),
    TXT("txt")
}

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

/**
 * Returns the path to the file, containing given ktElement.
 * Path is relative from the ktElement's project directory. )
 */
fun getRelativePathToKtElement(ktElement: KtElement): Path {
    val filePath = Paths.get(ktElement.containingKtFile.virtualFilePath)
    val projectPath = ktElement.project.basePath
        ?: throw IllegalArgumentException("Cannot find path to the project containing element ${ktElement.name}")
    val projectPathParent = Paths.get(projectPath).parent
    val fileRelativePath = projectPathParent.relativize(filePath)
    return fileRelativePath
}
