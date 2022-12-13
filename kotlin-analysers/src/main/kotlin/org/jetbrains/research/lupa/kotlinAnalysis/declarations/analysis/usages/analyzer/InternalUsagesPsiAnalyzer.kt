package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer

import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.base.util.module
import org.jetbrains.kotlin.idea.gradleJava.configuration.sourceSetName
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesAnalysisResult
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.isInternal

object InternalUsagesPsiAnalyzer : PsiAnalyzer<KtNamedDeclaration, List<InternalUsagesAnalysisResult>?> {

    private fun UsageInfo.getFqName(declarationFqName: String): InternalUsagesAnalysisResult? {
        this.element?.let { psi ->
            for (parent in psi.parents) {
                parent.kotlinFqName?.let {
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
        if (psiElement.isInternal()) {
            val processor = RenamePsiElementProcessor.forElement(psiElement)
            val references = processor.findReferences(psiElement, psiElement.useScope, false)
            val usages = references.map { UsageInfo(it) }.toTypedArray()
            return usages.mapNotNull { it.getFqName(psiElement.fqName.toString()) }
        }
        return null
    }
}
