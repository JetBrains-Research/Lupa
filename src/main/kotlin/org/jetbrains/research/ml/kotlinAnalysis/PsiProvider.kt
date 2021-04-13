package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class PsiProvider {

    fun extractMethodsFromProject(project: Project): MutableList<KtNamedFunction> {
        val ktMethods = mutableListOf<KtNamedFunction>()
        val ktFiles = extractPsiFiles(project)
        ktFiles.forEach { ktFile ->
            collectPsiMethods(ktFile).forEach { function ->
                ktMethods.add(function)
            }
        }
        return ktMethods
    }

    private fun extractPsiFiles(project: Project): MutableList<KtFile> {
        val projectPsiFiles = mutableListOf<KtFile>()
        ProjectRootManager.getInstance(project).contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if ((virtualFile.extension != "kt" && virtualFile.extension != "kts") ||
                    virtualFile.canonicalPath == null
                ) {
                    return@iterateChildrenRecursively true
                }
                val psi =
                    PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi as KtFile)
            }
        }
        return projectPsiFiles
    }

    private fun collectPsiMethods(ktFile: KtFile): MutableList<KtNamedFunction> {
        val filePsiMethods = mutableListOf<KtNamedFunction>()
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                filePsiMethods.add(function)
            }
        })
        return filePsiMethods
    }

    fun deleteComments(element: PsiElement) {
        val comments = PsiTreeUtil.collectElementsOfType(element, PsiComment::class.java)
        comments.forEach {
            WriteCommandAction.runWriteCommandAction(it.project) {
                it.delete()
            }
        }
    }
}
