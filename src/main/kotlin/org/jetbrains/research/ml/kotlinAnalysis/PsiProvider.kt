package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.*
import java.nio.file.Path

class PsiProvider {

    fun extractMethodsFromProject(projectPath : Path) {
        val project = ProjectUtil.openOrImport(projectPath, null, true) ?: return
        val ktFiles = extractPsiFiles(project)

        ktFiles.forEach { ktFile ->
            collectPsiMethods(ktFile).forEach { function ->
                println("${project.name}, ${ktFile.virtualFilePath}, ${function.name}, ${function.textRange}")
            }
        }
    }

    private fun extractPsiFiles(project : Project): MutableList<KtFile> {
        val projectPsiFiles = mutableListOf<KtFile>()
        ProjectRootManager.getInstance(project).contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if ((virtualFile.extension != "kt" && virtualFile.extension != "kts")
                    || virtualFile.canonicalPath == null) {
                    return@iterateChildrenRecursively true
                }
                val psi =
                    PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi as KtFile)
            }
        }
        return projectPsiFiles
    }

    private fun collectPsiMethods(ktFile: KtFile) : MutableList<KtNamedFunction> {
        val filePsiMethods = mutableListOf<KtNamedFunction>()
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                filePsiMethods.add(function)

            }
        })
        return filePsiMethods
    }
}
