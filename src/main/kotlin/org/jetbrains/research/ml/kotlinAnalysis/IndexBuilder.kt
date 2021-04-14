package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.nio.file.Path

/**
 * Builds incremental index of given project or method.
 * Index is printed to file in output directory.
 */
class IndexBuilder(outputDir: Path) : AutoCloseable {
    private var lastProjectId = 0
    private var lastMethodId = 0
    private val projectIndexWriter = printWriter(outputDir, "project_index.txt")
    private val methodIndexWriter = printWriter(outputDir, "method_index.csv")

    fun indexProject(project: Project): Int {
        projectIndexWriter.println("$lastProjectId,${project.basePath}")
        return lastProjectId++
    }

    fun indexMethod(function: KtNamedFunction, projectId: Int): Int {
        val filePath = function.containingKtFile.virtualFilePath
        val doc = PsiDocumentManager.getInstance(function.project).getDocument(function.containingFile)
            ?: throw IllegalArgumentException("Cannot find document containing function ${function.name}")
        val startLine = doc.getLineNumber(function.textRange.startOffset)
        val endLine = doc.getLineNumber(function.textRange.endOffset)
        methodIndexWriter.println(
            "$lastMethodId,$projectId,$filePath,$startLine,$endLine"
        )
        return lastMethodId++
    }

    override fun close() {
        projectIndexWriter.close()
        methodIndexWriter.close()
    }
}
