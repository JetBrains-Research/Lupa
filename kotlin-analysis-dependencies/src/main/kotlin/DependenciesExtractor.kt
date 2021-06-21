package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.research.ml.kotlinAnalysis.util.getSubdirectories
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Extracts and prints all dependencies from projects.
 */
class DependenciesExtractor {

    fun extractDependencies(inputDir: Path) {
        var nWithLibraries = 0
        var n = 0
        getSubdirectories(inputDir).forEach { projectPath ->
            ApplicationManager.getApplication().runReadAction {
                println("Opening project $projectPath")
                var project: Project? = null
                ApplicationManager.getApplication().invokeAndWait {
                    project = setUpProject(projectPath.toString())
                }
                require(project != null) { "Did not set up the project with the path $inputDir" }
                // there can be some internal IDE errors during project processing (especially opening),
                // but anyway the project has to be closed
                try {
                    var flag = false
                    n++

                    ModuleManager.getInstance(project!!).modules.forEach {
                        println("Module name: " + it.name)
                        println("Module's libs")
                        ModuleRootManager.getInstance(it).orderEntries().forEachLibrary { lib ->
                            flag = true
                            println("Library " + lib.name)
                            true
                        }
                    }
                    if (flag) {
                        nWithLibraries++
                    }
                    println("Found $nWithLibraries projects with any dependencies out of $n")
                } catch (ex: Exception) {
                    println(ex.message)
                } finally {
                    ApplicationManager.getApplication().invokeAndWait {
                        ProjectManagerEx.getInstanceEx().forceCloseProject(project!!)
                    }
                }
            }
        }
    }

    private fun setUpProject(projectPath: String): Project {
        val project: Project = ProjectUtil.openOrImport(Paths.get(projectPath))

        if (MavenProjectsManager.getInstance(project).isMavenizedProject) {
            MavenProjectsManager.getInstance(project).scheduleImportAndResolve()
            MavenProjectsManager.getInstance(project).importProjects()
        } else {
            ExternalSystemUtil.refreshProject(
                projectPath,
                ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
            )
        }

        return project
    }
}
