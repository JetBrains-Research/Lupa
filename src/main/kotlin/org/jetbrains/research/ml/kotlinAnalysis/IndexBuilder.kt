package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.core.util.end
import org.jetbrains.kotlin.idea.core.util.start
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
        val range = function.textRange
        methodIndexWriter.println("$lastMethodId,$projectId,$filePath,${range.start},${range.end}")
        return lastMethodId++
    }
}
