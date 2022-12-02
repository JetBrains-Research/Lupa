package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.asJava.unwrapped
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.refactoring.isInterfaceClass
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

data class StdlibInterfacesAnalysisResult(
    val fqName: String,
    val filePath: String,
    val position: Int,
)

object StdlibInterfacesAnalyzer : PsiAnalyzer<KtClass, List<StdlibInterfacesAnalysisResult>?> {
    private val baseInterfaces = listOf(
        "ContinuationInterceptor",
        "CoroutineContext",
        "CoroutineContext.Element",
        "ClosedRange",
        "OpenEndRange",
        "ClosedFloatingPointRange",
        "kotlin.text.MatchResult",
        "TimeMark",
        "ComparableTimeMark",
    )

    private fun KtClass.getSuperTypes(): List<String> {
        val supertypes = mutableListOf<String>()
        val directSupertypes = this.superTypeListEntries
        // TODO: Get not the current name
        val a = this.getChildOfType<KtTypeReference>()
        supertypes.addAll(directSupertypes.mapNotNull{ (it.getChildOfType<KtNameReferenceExpression>()?.resolve() as? KtClass)?.fqName?.asString() })
        directSupertypes.forEach { t ->
            (t.typeAsUserType?.referenceExpression?.resolve() as? KtClass)?.getSuperTypes()?.let {
                supertypes.addAll(it)
            }
        }
        return supertypes
    }

    override fun analyze(psiElement: KtClass): List<StdlibInterfacesAnalysisResult>? {
        if (psiElement.isInterface()) {
            val supertypes = psiElement.getSuperTypes()
            println(supertypes)
        }
        return null
//        TODO("Not yet implemented")
    }
}
