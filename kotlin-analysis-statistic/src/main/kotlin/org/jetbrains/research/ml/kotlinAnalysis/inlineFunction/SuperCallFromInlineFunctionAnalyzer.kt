package org.jetbrains.research.ml.kotlinAnalysis.inlineFunction

import org.jetbrains.kotlin.idea.util.hasInlineModifier
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

object SuperCallFromInlineFunctionAnalyzer : PsiAnalyzer<KtSuperExpression, Boolean> {

    override fun analyze(psiElement: KtSuperExpression): Boolean {
        var curElement = psiElement.parent
        while (curElement !is KtFile) {
            if (curElement is KtNamedFunction) {
                val curElementModifiersOwner = curElement as KtModifierListOwner

                val isInline = curElementModifiersOwner.hasInlineModifier()
                val isPublicOrProtected = curElementModifiersOwner.isPublic || curElementModifiersOwner.isProtected()
                val isClassPrivateOrInternal = curElement.containingClass()?.let { isClassPrivateOrInternal(it) }
                return isInline && isPublicOrProtected && isClassPrivateOrInternal != null && !isClassPrivateOrInternal
            }
            curElement = curElement.parent
        }
        return false
    }

    private fun isClassPrivateOrInternal(psiElement: KtClass): Boolean {
        return (psiElement as KtModifierListOwner).isPrivate() ||
                psiElement.modifierList?.visibilityModifier()?.textMatches("internal") ?: false
    }
}
