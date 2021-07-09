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
import org.jetbrains.research.ml.kotlinAnalysis.util.isKotlinRelatedFile

/**
 * Provides methods based on interaction with PSI, for example, extraction of all psi elements of specific
 * type from project or deleting comments.
 */
object PsiProvider {

    fun <T : PsiElement> extractElementsOfTypeFromProject(
        project: Project,
        psiElementClass: Class<T>
    ): List<T> {
        return extractPsiFiles(project)
            .map { PsiTreeUtil.collectElementsOfType(it, psiElementClass) }
            .flatten()
    }

    fun deleteComments(element: PsiElement) {
        val comments = PsiTreeUtil.collectElementsOfType(element, PsiComment::class.java)
        // We should handle each group of elements with same parent separately
        // since if we delete the first item in this group we invalidate
        // other elements with the same parent
        comments.toList().groupBy { it.parent }.entries.forEach {
            // If we do not change the order of the elements, then a parent can invalidate
            // the child element, but it can also be a comment and an exception will be thrown,
            // so we must delete the found comments in the reverse order
            it.value.reversed().forEach { comment ->
                WriteCommandAction.runWriteCommandAction(comment.project) {
                    comment.delete()
                }
            }
        }
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
}
