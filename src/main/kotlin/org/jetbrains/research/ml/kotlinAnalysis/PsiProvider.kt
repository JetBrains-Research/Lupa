package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * Provides methods based on interaction with PSI, for example, extraction of all methods from project.
 */
object PsiProvider {

    fun extractMethodsFromProject(project: Project): List<KtNamedFunction> {
        return extractPsiFiles(project).map { collectPsiMethods(it) }.flatten()
    }

    private fun extractPsiFiles(project: Project): MutableList<PsiFile> {
        val projectPsiFiles = mutableListOf<PsiFile>()
        val projectRootManager = ProjectRootManager.getInstance(project)
        val psiManager = PsiManager.getInstance(project)

        projectRootManager.contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if (!virtualFile.isKotlinRelatedFile() || virtualFile.canonicalPath == null) {
                    return@iterateChildrenRecursively true
                }
                val psi = psiManager.findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi)
            }
        }
        return projectPsiFiles
    }

    private fun collectPsiMethods(psiFile: PsiFile): MutableList<KtNamedFunction> {
        val filePsiMethods = mutableListOf<KtNamedFunction>()
        psiFile.accept(object : KtTreeVisitorVoid() {
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
