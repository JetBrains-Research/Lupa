package org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.psi.PyElement
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.research.lupa.kotlinAnalysis.util.isKotlinRelatedFile
import org.jetbrains.research.lupa.kotlinAnalysis.util.isPythonRelatedFile
import java.nio.file.Paths
import java.util.*

/** File contains various extension methods for [com.intellij.openapi.project.Project] class. */

/** Extracts elements of given type from files in project. */
fun <T : PsiElement> Project.extractElementsOfType(
    psiElementClass: Class<T>,
    filePredicate: (VirtualFile) -> Boolean = VirtualFile::isKotlinRelatedFile,
): List<T> {
    return extractPsiFiles(filePredicate)
        .map { it.extractElementsOfType(psiElementClass) }
        .flatten()
}

/** Extracts [kotlin elements][KtElement] of given type from kotlin related files in project. */
fun <T : KtElement> Project.extractKtElementsOfType(psiElementClass: Class<T>): List<T> {
    return extractElementsOfType(psiElementClass, VirtualFile::isKotlinRelatedFile)
}

/** Extracts [python elements][PyElement] of given type from python related files in project. */
fun <T : PyElement> Project.extractPyElementsOfType(
    psiElementClass: Class<T>,
    ignoreVenvFolder: Boolean = true,
): List<T> {
    return extractElementsOfType(psiElementClass) { virtualFile -> virtualFile.isPythonRelatedFile(ignoreVenvFolder) }
}

/** Extracts all modules from project. */
fun Project.extractModules(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}

/** Extracts root module from project. */
fun Project.extractRootModule(): Module? {
    return extractModules().firstOrNull { it.name == this.name }
}

/**
 * Extracts files from project matching given predicate.
 * For example all kotlin related files.
 */
fun Project.extractPsiFiles(filePredicate: (VirtualFile) -> Boolean = VirtualFile::isKotlinRelatedFile):
        MutableSet<PsiFile> {
    val projectPsiFiles = mutableSetOf<PsiFile>()
    val projectRootManager = ProjectRootManager.getInstance(this)
    val psiManager = PsiManager.getInstance(this)

    projectRootManager.contentRoots.mapNotNull { root ->
        VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
            if (!filePredicate(virtualFile) || virtualFile.canonicalPath == null) {
                return@iterateChildrenRecursively true
            }
            val psi = psiManager.findFile(virtualFile) ?: return@iterateChildrenRecursively true
            projectPsiFiles.add(psi)
        }
    }
    return projectPsiFiles
}

/**
 * Finds first file in breadth-first traversal order from project matching given predicate.
 * For example to find gradle or manifest files.
 */
fun Project.findPsiFile(filePredicate: (VirtualFile) -> Boolean = VirtualFile::isKotlinRelatedFile): PsiFile? {
    var subFiles = basePath
        ?.let { VfsUtil.findFile(Paths.get(it), false) }
        ?.let { mutableListOf(it) } ?: return null

    val psiManager = PsiManager.getInstance(this)

    while (subFiles.isNotEmpty()) {
        val file = subFiles.firstOrNull(filePredicate)
        if (file != null) {
            return psiManager.findFile(file)
        } else {
            subFiles = subFiles.flatMap { it.children.toList() }.toMutableList()
        }
    }
    return null
}
