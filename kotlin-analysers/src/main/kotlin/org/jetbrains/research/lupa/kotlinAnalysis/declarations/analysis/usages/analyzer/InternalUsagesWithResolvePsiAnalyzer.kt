package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.analyzer

import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.base.util.module
import org.jetbrains.kotlin.idea.gradleJava.configuration.sourceSetName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer
import org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.usages.InternalUsagesAnalysisResult
import org.jetbrains.research.lupa.kotlinAnalysis.psi.extentions.isInternal

object InternalUsagesWithResolvePsiAnalyzer :
    PsiAnalyzer<KtNameReferenceExpression, List<InternalUsagesAnalysisResult>?> {

    private fun KtNameReferenceExpression.fqName(): FqName? =
        this.parents.find { it.kotlinFqName != null }?.kotlinFqName

    private fun KtNamedDeclaration.isInternalDeclaration(): Boolean =
        this.isInternal() || this.hasInternalParent()

    private fun KtNamedDeclaration.hasInternalParent() = getInternalParent()?.isInternal() ?: false

    private fun KtNamedDeclaration.getInternalParent(): KtNamedDeclaration? {
        when (this) {
            is KtClassOrObject, is KtNamedFunction -> return this.parents.mapNotNull { it as? KtNamedDeclaration }
                .find { it.isInternal() }
        }
        return null
    }

    private fun KtNamedDeclaration.fqName(): FqName? {
        if (this.isInternal()) {
            return this.fqName
        }
        return this.getInternalParent()?.fqName
    }

    override fun analyze(psiElement: KtNameReferenceExpression): List<InternalUsagesAnalysisResult>? {
        val resolvedReferencesFqNames = psiElement.references.asSequence()
            .mapNotNull { it.resolve() }
            .mapNotNull { it as? KtNamedDeclaration }
            .filter { it.isInternalDeclaration() }
            .mapNotNull { it.fqName() }
            .toList()

        psiElement.fqName()?.let { fqName ->
            return resolvedReferencesFqNames.map {
                InternalUsagesAnalysisResult(
                    it.toString(),
                    fqName.toString(),
                    psiElement.module?.name,
                    psiElement.module?.sourceSetName,
                )
            }
        }

        return null
    }
}
