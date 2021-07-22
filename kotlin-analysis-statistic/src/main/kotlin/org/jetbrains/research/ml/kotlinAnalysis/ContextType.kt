package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

enum class ContextType(val klass: Class<out KtElement>?) {
    FOR(KtForExpression::class.java),
    WHILE(KtWhileExpression::class.java),
    IF(KtIfExpression::class.java),
    WHEN(KtWhenExpression::class.java),
    PROPERTY(KtProperty::class.java),
    FOREACH(null),
    MAP(null),
    TOLIST(null),
    REQUIRE(null),
    OTHER(null);

    companion object {
        private val mapClasses: Map<Class<out KtElement>?, ContextType> = values().associateBy(ContextType::klass)
        fun fromElement(element: PsiElement): ContextType? {
            return mapClasses
                .getOrDefault<Class<out PsiElement>?, ContextType?>(element::class.java, null)
        }
    }
}
