package org.jetbrains.research.ml.kotlinAnalysis.psi.extentions

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/** Extracts elements of given type from [PsiElement] subtree. */
fun <T : PsiElement> PsiElement.extractElementsOfType(psiElementClass: Class<T>): MutableCollection<T> =
    PsiTreeUtil.collectElementsOfType(this, psiElementClass)

/** Deletes comments [PsiComment] from from given [PsiElement]. */
fun PsiElement.deleteComments() {
    val comments = this.extractElementsOfType(PsiComment::class.java)
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
