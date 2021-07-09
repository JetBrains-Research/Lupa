package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Builds incremental index of given project or method.
 * Index is printed to file in output directory.
 */
class IndexBuilder(outputDir: Path) : ResourceManager {
    private var lastProjectId = 0
    private var lastMethodId = 0
    private val projectIndexWriter = PrintWriterRecourseManager(outputDir, "project_index.csv")
    private val methodIndexWriter = PrintWriterRecourseManager(outputDir, "method_index.csv")

    fun indexProject(project: Project): Int {
        projectIndexWriter.writer.println("$lastProjectId\t${project.name}")
        return lastProjectId++
    }

    fun indexMethod(function: KtNamedFunction, projectId: Int): Int {
        val filePath = Paths.get(function.containingKtFile.virtualFilePath)
        val projectPath = function.project.basePath
            ?: throw IllegalArgumentException("Cannot find path to the project containing function ${function.name}")
        val projectPathParent = Paths.get(projectPath).parent
        val fileRelativePath = projectPathParent.relativize(filePath)

        val doc = PsiDocumentManager.getInstance(function.project).getDocument(function.containingFile)
            ?: throw IllegalArgumentException("Cannot find document containing function ${function.name}")
        val startLine = doc.getLineNumber(function.textRange.startOffset)
        val endLine = doc.getLineNumber(function.textRange.endOffset)
        methodIndexWriter.writer.println(
            "$projectId\t$lastMethodId\t$fileRelativePath\t$startLine\t$endLine"
        )
        return lastMethodId++
    }

    override fun init() {
        projectIndexWriter.init()
        methodIndexWriter.init()
    }

    override fun close() {
        projectIndexWriter.close()
        methodIndexWriter.close()
    }
}
