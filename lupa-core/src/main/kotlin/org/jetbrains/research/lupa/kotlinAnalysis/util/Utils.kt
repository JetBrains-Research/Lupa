package org.jetbrains.research.lupa.kotlinAnalysis.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createDirectories
import com.intellij.util.io.isDirectory
import com.intellij.util.io.isFile
import java.io.File
import java.io.PrintWriter
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.relativeTo
import kotlin.streams.toList

enum class KotlinConstants(val value: String) {
    OGR_JETBRAINS_KOTLIN("org.jetbrains.kotlin"),
    KOTLIN("kotlin")
}

enum class FileExtension(val value: String) {
    KT("kt"),
    KTS("kts"),
    GRADLE("gradle"),
    PROPERTIES("properties"),
    PY("py");

    companion object {
        private val mapExtension = values().associateBy(FileExtension::value)
        fun fromValue(type: String) = mapExtension[type]
    }
}

val KOTLIN_EXTENSIONS = setOf(FileExtension.KT, FileExtension.KTS)
val PYTHON_EXTENSIONS = setOf(FileExtension.PY)

const val PYTHON_VENV_FOLDER_NAME = ".venv"

fun VirtualFile.isKotlinRelatedFile(): Boolean {
    return this.extension == FileExtension.KT.value || this.extension == FileExtension.KTS.value
}

fun VirtualFile.isPythonRelatedFile(ignoreVenvFolder: Boolean = true): Boolean =
    this.extension == FileExtension.PY.value && !(ignoreVenvFolder && this.isPythonVirtualEnvironmentFile())

fun VirtualFile.isPythonVirtualEnvironmentFile(): Boolean =
    this.canonicalPath?.contains("${File.separator}$PYTHON_VENV_FOLDER_NAME${File.separator}") ?: false


fun requireDirectory(path: Path) {
    require(path.isDirectory()) { "Argument has to be directory" }
}

fun getSubdirectories(path: Path): List<Path> {
    return Files.walk(path, 1)
        .filter { Files.isDirectory(it) && !it.equals(path) }
        .toList()
}

fun getFilesWithExtensions(path: Path, extensions: Set<FileExtension>): List<Path> {
    return Files.walk(path, FileVisitOption.FOLLOW_LINKS)
        .filter {
            it.isFile() && (extensions.isEmpty() || extensions.contains(FileExtension.fromValue(it.extension))) &&
                    !it.equals(path)
        }
        .toList()
}

fun symbolicCopyOnlyRequiredExtensions(fromDirectory: Path, toDirectory: Path, extensions: Set<FileExtension>) {
    getFilesWithExtensions(fromDirectory, extensions).forEach { filePath ->
        val tempFilePath = Paths.get(
            toDirectory.toString(),
            filePath.relativeTo(fromDirectory).toString()
        )
        tempFilePath.parent.createDirectories()
        Files.createSymbolicLink(tempFilePath, filePath)
    }
}

fun getPrintWriter(directory: Path, fileName: String): PrintWriter {
    directory.createDirectories()
    val file = File(directory.toFile(), fileName)
    return file.printWriter()
}
