package org.jetbrains.research.ml.kotlinAnalysis.psi.extentions

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex

/** Finds [PsiFile] in module by given name. */
fun Module.findPsiFileByName(name: String): PsiFile? {
    return FilenameIndex.getFilesByName(project, name, moduleContentScope).firstOrNull()
}

/** Finds [PsiFile] in module by given file extension. */
fun Module.findPsiFilesByExtension(extension: String): List<PsiFile> {
    val psiManager = PsiManager.getInstance(project)
    return FilenameIndex.getAllFilesByExt(project, extension, moduleContentScope)
        .mapNotNull { psiManager.findFile(it) }
        .toList()
}
