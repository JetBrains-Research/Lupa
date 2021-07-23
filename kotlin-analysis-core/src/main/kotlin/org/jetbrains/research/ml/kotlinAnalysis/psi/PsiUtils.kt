package org.jetbrains.research.ml.kotlinAnalysis.psi

import org.jetbrains.kotlin.psi.KtElement
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Returns the path to the file, containing given ktElement.
 * Path is relative from the ktElement's project directory. )
 */
fun KtElement.getRelativePathToKtElement(): Path {
    val filePath = Paths.get(this.containingKtFile.virtualFilePath)
    val projectPath = this.project.basePath
        ?: throw IllegalArgumentException("Cannot find path to the project containing element ${this.name}")
    val projectPathParent = Paths.get(projectPath).parent
    val fileRelativePath = projectPathParent.relativize(filePath)
    return fileRelativePath
}
