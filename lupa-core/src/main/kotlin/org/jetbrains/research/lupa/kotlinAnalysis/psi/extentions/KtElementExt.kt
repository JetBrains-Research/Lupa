package org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.util.getRelativePath

/**
 * Returns the path to the file, containing given ktElement.
 * Path is relative from the ktElement's project directory. )
 */
fun KtElement.getRelativePathToKtElement() = this.containingKtFile.virtualFilePath.getRelativePath(this.project)

fun KtNamedDeclaration.isInternal() = hasModifier(KtTokens.INTERNAL_KEYWORD)
