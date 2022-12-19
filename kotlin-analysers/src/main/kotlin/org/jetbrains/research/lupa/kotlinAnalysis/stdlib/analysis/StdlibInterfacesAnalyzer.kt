package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor
import org.jetbrains.research.lupa.kotlinAnalysis.PsiAnalyzer

data class StdlibInterfacesAnalysisResult(
    val functionName: String,
    val interfaceName: String,
    val baseInterfaces: List<String>,
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

    private fun KtClass.getSuperTypes(): List<ClassDescriptor> =
        this.resolveToDescriptorIfAny()?.getAllSuperClassifiers()?.filterIsInstance<ClassDescriptor>()?.filter {
            it.fqNameOrNull().toString() in baseInterfaces
        }?.toList() ?: error("Can not resolve descriptor for class ${this.name}")

    private fun FunctionDescriptor.overriddenFqNames() =
        overriddenDescriptors.map { it.containingDeclaration.fqNameSafe.toString() }
            .filter { it in baseInterfaces }

    private fun FunctionDescriptor.containsSuperCalls(): Boolean {
        if (overriddenFqNames().isEmpty()) {
            return false
        }

        val superCall = "super.${this.name}("
        return this.psiElement?.text?.contains(superCall) ?: false
    }

    override fun analyze(psiElement: KtClass): List<StdlibInterfacesAnalysisResult>? {
        if (psiElement.isInterface()) {
            val supertypes = psiElement.getSuperTypes()
            if (supertypes.isNotEmpty()) {
                val functions =
                    (psiElement.resolveToDescriptorIfAny() as LazyClassDescriptor)
                        .declaredCallableMembers
                        .filterIsInstance<FunctionDescriptor>()
                        .filter { it.containsSuperCalls() }

                if (functions.isEmpty()) {
                    return null
                }

                return functions.map {
                    StdlibInterfacesAnalysisResult(
                        interfaceName = psiElement.fqName?.asString() ?: "Anonymous interface",
                        functionName = it.name.toString(),
                        baseInterfaces = it.overriddenFqNames(),
                    )
                }
            }
            return null
        }
        return null
    }
}
