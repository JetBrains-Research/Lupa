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
import org.slf4j.LoggerFactory

data class StdlibInterfacesAnalysisResult(
    val functionName: String,
    val interfaceName: String,
    val baseInterfaces: List<String>,
    val hasSuperOverrides: Boolean,
)

object StdlibInterfacesAnalyzer : PsiAnalyzer<KtClass, List<StdlibInterfacesAnalysisResult>?> {
    private val logger = LoggerFactory.getLogger(javaClass)

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

    private fun KtClass.hasSuperTypes(): Boolean =
        this.resolveToDescriptorIfAny()?.getAllSuperClassifiers()?.filterIsInstance<ClassDescriptor>()?.filter {
            it.fqNameOrNull().toString() in baseInterfaces
        }?.toList()?.isNotEmpty() ?: error("Can not resolve descriptor for class ${this.name}")

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
        logger.info("Start analyzing ${psiElement.name} class")
        if (!psiElement.isInterface()) {
            logger.info("It is not an interface")
            return null
        }
        if (!psiElement.hasSuperTypes()) {
            logger.info("This class does not have any necessary supertypes")
            return null
        }
        logger.info("${psiElement.name} is valid, start checking functions....")

        val functions =
            (psiElement.resolveToDescriptorIfAny() as LazyClassDescriptor)
                .declaredCallableMembers
                .filterIsInstance<FunctionDescriptor>()
                .filter { it.containsSuperCalls() }

        return functions.map {
            StdlibInterfacesAnalysisResult(
                interfaceName = psiElement.fqName?.asString() ?: "Anonymous interface",
                functionName = it.name.toString(),
                baseInterfaces = it.overriddenFqNames(),
                hasSuperOverrides = functions.isNotEmpty(),
            )
        }
    }
}
