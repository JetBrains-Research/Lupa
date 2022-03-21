package org.jetbrains.research.lupa.kotlinAnalysis.util.python.jupyter

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

fun copyDataset(datasetDir: Path, targetDir: Path) {
    try {
        Files.createDirectory(targetDir)
    } catch (e: IOException) {
        System.err.println("Directory already exists.")
    } finally {
        File(datasetDir.toString()).copyRecursively(File(targetDir.toString()), overwrite = true)
    }
}

fun readFileAsText(filename: String): String {
    return File(filename).readText()
}
