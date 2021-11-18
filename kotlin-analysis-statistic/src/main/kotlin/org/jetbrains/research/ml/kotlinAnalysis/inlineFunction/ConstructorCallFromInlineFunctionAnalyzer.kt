package org.jetbrains.research.ml.kotlinAnalysis.inlineFunction

import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.idea.util.hasInlineModifier
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.research.ml.kotlinAnalysis.PsiAnalyzer

object ConstructorFromInlineFunctionAnalyzer : PsiAnalyzer<KtCallExpression, Boolean> {

    override fun analyze(psiElement: KtCallExpression): Boolean {
        if (!psiElement.isConstructorCall()) return false

        // check whether at least one of the class constructors has protected modifier
        val classHasProtectedConstructor =
            psiElement.containingClass()!!.allConstructors.map { it.isProtected() }.any { it }
        if (!classHasProtectedConstructor) return false

        // check whether method containing this call expression has inline and public modifiers
        var curElement = psiElement.parent
        while (curElement !is KtFile) {
            if (curElement is KtNamedFunction) {
                val curElementModifiersOwner = curElement as KtModifierListOwner
                return (curElementModifiersOwner.hasInlineModifier() && curElementModifiersOwner.isPublic)
            }
            curElement = curElement.parent
        }
        return false
    }

    fun KtCallExpression.isConstructorCall(): Boolean {
        val className = this.containingClass()?.name
        val methodName = this.callName()
        return className != null && className == methodName
    }
}
