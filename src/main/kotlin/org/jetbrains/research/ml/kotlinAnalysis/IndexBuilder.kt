package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.PrintWriter

class IndexBuilder {
    private var lastProjectId = 0
    private var lastMethodId = 0

    fun indexProject(project: Project, projectIndexWriter: PrintWriter): Int {
        projectIndexWriter.println("$lastProjectId,${project.basePath}")
        return lastProjectId++
    }

    fun indexMethod(function: KtNamedFunction, projectId: Int, methodIndexWriter: PrintWriter): Int {
        val filePath = function.containingKtFile.virtualFilePath
        val doc = PsiDocumentManager.getInstance(function.project).getDocument(function.containingFile)
        val startLine = doc?.getLineNumber(function.textRange.startOffset)
        val endLine = doc?.getLineNumber(function.textRange.endOffset)
        methodIndexWriter.println(
            "$lastMethodId,$projectId,$filePath,$startLine,$endLine"
        )
        return lastMethodId++
    }
}
