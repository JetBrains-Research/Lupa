package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import org.jetbrains.research.ml.kotlinAnalysis.util.getPrintWriter
import org.jetbrains.research.ml.kotlinAnalysis.util.getSubdirectories
import java.nio.file.Path

/**
 * Extracts all import directives from projects in the dataset and saves them to output file.
 */
class ImportDirectivesMiner(outputDir: Path) {
    private val methodDataWriter = getPrintWriter(outputDir, "import_directives_data.txt")

    fun extractImportDirectives(inputDir: Path) {
        try {
            getSubdirectories(inputDir).forEach { projectPath ->
                ApplicationManager.getApplication().runReadAction {
                    println("Opening project $projectPath")
                    ProjectUtil.openOrImport(projectPath, null, true).let { project ->
                        try {
                            val importDirectives = PsiProvider.extractImportDirectiveFromProject(project)
                            val results = importDirectives.map { ImportDirectiveAnalyzer.analyze(it) }
                            methodDataWriter.println(results.joinToString(separator = "\n"))
                        } catch (ex: Exception) {
                            println(ex.message)
                        } finally {
                            ApplicationManager.getApplication().invokeAndWait {
                                ProjectManagerEx.getInstanceEx().forceCloseProject(project)
                            }
                        }
                    }
                }
            }
        } finally {
            close()
        }
    }

    private fun close() {
        methodDataWriter.close()
    }
}
