package org.jetbrains.research.ml.kotlinAnalysis.psi

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
import org.jetbrains.research.ml.kotlinAnalysis.util.isKotlinRelatedFile

/**
 * Provides methods based on interaction with PSI, for example, extraction of all methods from project.
 */
object PsiProvider {

    fun extractMethodsFromProject(project: Project): Set<KtNamedFunction> {
        return extractPsiFiles(project).map { collectPsiMethods(it) }.flatten().toSet()
    }

    private fun extractPsiFiles(project: Project): MutableSet<PsiFile> {
        val projectPsiFiles = mutableSetOf<PsiFile>()
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

    private fun collectPsiMethods(psiFile: PsiFile): MutableCollection<KtNamedFunction> {
        return PsiTreeUtil.collectElementsOfType(psiFile, KtNamedFunction::class.java)
    }

    fun deleteComments(element: PsiElement) {
        val comments = PsiTreeUtil.collectElementsOfType(element, PsiComment::class.java)
        // If we do not change the order of the elements, then a parent can invalidate
        // the child element, but it can also be a comment and an exception will be thrown,
        // so we must delete the found comments in the reverse order
        comments.reversed().forEach {
            WriteCommandAction.runWriteCommandAction(it.project) {
                it.delete()
            }
        }
    }
}
