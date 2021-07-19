package org.jetbrains.research.ml.kotlinAnalysis.psi.extentions

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.gradle.GradleFileManager
import org.jetbrains.research.ml.kotlinAnalysis.util.isKotlinRelatedFile

fun Project.isAndroidProject(): Boolean {
    return extractRootModule()?.let {
        GradleFileManager.extractGradleFileFromModule(it)?.extractBuildGradleDependencyByName("com.android.tools")
    } != null
}

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

fun <T : PsiElement> Project.extractElementsOfType(
    psiElementClass: Class<T>
): List<T> {
    return extractPsiFiles()
        .map { it.extractElementsOfType(psiElementClass) }
        .flatten()
}

fun Project.extractModules(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}

fun Project.extractRootModule(): Module? {
    return extractModules().firstOrNull() { it.name == this.name || it.name == this.name.replace(" ", "_") }
}
