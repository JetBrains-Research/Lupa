package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis

import com.intellij.openapi.diagnostic.Logger
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
    private val logger: Logger = Logger.getInstance(StdlibInterfacesAnalyzer::class.java)

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
        if (!psiElement.isInterface() || !psiElement.hasSuperTypes()) {
            return null
        }
        logger.info("${psiElement.name} is valid, start checking functions....")

        val functions =
            (psiElement.resolveToDescriptorIfAny() as LazyClassDescriptor)
                .declaredCallableMembers
                .filterIsInstance<FunctionDescriptor>()
                .filter { it.containsSuperCalls() }

        if (functions.isEmpty()) {
            logger.info("Functions list with super calls is empty")
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
}
