package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.ml.kotlinAnalysis.util.getSubdirectories
import java.nio.file.Path

/**
 * Abstract class for analysis executor which provides interface for execution analysis
 * for each project in given dataset.
 */
abstract class AnalysisExecutor {

    /** Executes the analysis of the given [project][Project]. */
    abstract fun analyse(project: Project)

    /**
     * Finishes the task execution. Should contain close functions of all writers and readers
     * and called after all project analysis execution finished.
     */
    abstract fun finish()

    /** Execute analysis for all projects in [given directory][projectsDir]. */
    fun execute(projectsDir: Path) {
        try {
            getSubdirectories(projectsDir).forEach { projectPath ->
                ApplicationManager.getApplication().runReadAction {
                    println("Opening project $projectPath")
                    ProjectUtil.openOrImport(projectPath, null, true).let { project ->
                        try {
                            analyse(project)
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
            finish()
        }
    }
}
