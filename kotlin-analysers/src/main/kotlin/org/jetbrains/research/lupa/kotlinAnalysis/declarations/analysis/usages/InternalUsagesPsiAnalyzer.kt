package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages

import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import org.jetbrains.kotlin.idea.configuration.sourceSetName
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

data class InternalUsagesAnalysisResult(
    val declarationFqName: String,
    val usageFqName: String,
    val moduleName: String?,
    val sourceSet: String?,
)

object InternalUsagesPsiAnalyzer : PsiAnalyzer<KtNamedDeclaration, List<InternalUsagesAnalysisResult>?> {

    private fun UsageInfo.getFqName(declarationFqName: String): InternalUsagesAnalysisResult? {
        this.element?.let { psi ->
            for (parent in psi.parents) {
                parent.getKotlinFqName()?.let {
                    return InternalUsagesAnalysisResult(
                        declarationFqName,
                        it.toString(),
                        parent.module?.name,
                        parent.module?.sourceSetName,
                    )
                }
            }
        }
        return null
    }

    override fun analyze(psiElement: KtNamedDeclaration): List<InternalUsagesAnalysisResult>? {
        if (psiElement.hasModifier(KtTokens.INTERNAL_KEYWORD)) {
            val processor = RenamePsiElementProcessor.forElement(psiElement)
            val references = processor.findReferences(psiElement, psiElement.useScope, false)
            val usages = references.map { UsageInfo(it) }.toTypedArray()
            return usages.mapNotNull { it.getFqName(psiElement.fqName.toString()) }
        }
        return null
    }
}
