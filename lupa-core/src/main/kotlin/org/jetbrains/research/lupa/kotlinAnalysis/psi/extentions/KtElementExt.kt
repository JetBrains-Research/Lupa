package org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.research.lupa.kotlinAnalysis.util.getRelativePath

/**
 * Returns the path to the file, containing given ktElement.
 * Path is relative from the ktElement's project directory. )
 */
fun KtElement.getRelativePathToKtElement() = this.containingKtFile.virtualFilePath.getRelativePath(this.project)
