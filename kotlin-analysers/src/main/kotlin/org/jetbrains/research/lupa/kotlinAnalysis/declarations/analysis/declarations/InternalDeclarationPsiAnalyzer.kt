package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.declarations

import org.jetbrains.kotlin.idea.configuration.sourceSetName
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

data class InternalDeclarationAnalysisResult(
    val fqName: String,
    val isExpect: Boolean,
    val isActual: Boolean,
    val moduleName: String?,
    val sourceSet: String?,
)

object InternalDeclarationPsiAnalyzer : PsiAnalyzer<KtNamedDeclaration, InternalDeclarationAnalysisResult?> {

    override fun analyze(psiElement: KtNamedDeclaration): InternalDeclarationAnalysisResult? {
        if (psiElement.hasModifier(KtTokens.INTERNAL_KEYWORD)) {
            val isExpect = psiElement.hasModifier(KtTokens.EXPECT_KEYWORD)
            val isActual = psiElement.hasModifier(KtTokens.ACTUAL_KEYWORD)
            return InternalDeclarationAnalysisResult(
                psiElement.fqName.toString(),
                isExpect,
                isActual,
                psiElement.module?.name,
                psiElement.module?.sourceSetName,
            )
        }
        return null
    }
}
