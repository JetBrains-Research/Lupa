package org.jetbrains.research.ml.kotlinAnalysis.util

import com.intellij.openapi.project.Project
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import java.nio.file.Path

class RepositoryOpenerUtil {

    companion object {

        fun openReloadRepositoryOpener(path: Path, action: (Project) -> Unit) {
            var projectIndex = 0
            if (getKotlinJavaRepositoryOpener().openRepository(
                    path.toFile()
                ) { project ->
                    println("Opening project ${project.name} index=$projectIndex time=${System.currentTimeMillis()}")
                    action(project)
                    projectIndex += 1
                }
            ) {
                println("Not all projects from $path was opened successfully")
            }
        }
    }
}
