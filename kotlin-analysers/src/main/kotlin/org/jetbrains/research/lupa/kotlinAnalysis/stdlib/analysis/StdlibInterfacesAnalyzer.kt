package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

data class StdlibInterfacesAnalysisResult(
    val fqName: String,
    val filePath: String,
    val position: Int,
)

object StdlibInterfacesAnalyzer : PsiAnalyzer<KtClass, List<StdlibInterfacesAnalysisResult>?> {
    private val baseInterfaces = listOf(
        "kotlin.coroutines.ContinuationInterceptor",
        "kotlin.coroutines.CoroutineContext",
        "kotlin.coroutines.CoroutineContext.Element",
        "kotlin.ranges.ClosedRange",
        "kotlin.ranges.OpenEndRange",
        "kotlin.ranges.ClosedFloatingPointRange",
        "kotlin.text.MatchResult",
        "kotlin.time.TimeMark",
        "kotlin.time.ComparableTimeMark",
    )

    private fun KtClass.getSuperTypes(): List<String> =
        this.resolveToDescriptorIfAny()?.getAllSuperClassifiers()?.filterIsInstance<ClassDescriptor>()
            ?.map { it.fqNameOrNull() }?.map { it.toString() }?.toList()
            ?: error("Can not resolve descriptor for class ${this.name}")

    override fun analyze(psiElement: KtClass): List<StdlibInterfacesAnalysisResult>? {
        if (psiElement.isInterface()) {
            val supertypes = psiElement.getSuperTypes().filter { it in baseInterfaces }
            println("${psiElement.text}: $supertypes")
            // TODO: check <super> calls
        }
        return null
//        TODO("Not yet implemented")
    }
}
