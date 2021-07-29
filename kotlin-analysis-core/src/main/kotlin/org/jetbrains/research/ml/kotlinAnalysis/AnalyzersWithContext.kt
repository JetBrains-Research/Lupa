package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor

typealias AnalyzerWithContextToStat<C, R> = Map<PsiAnalyzerWithContext<C, R>, MutableMap<PsiElement, R>>

/** Cast Psi element to type [P] of given [pClass]. */
private fun <P : PsiElement> PsiElement.castToClass(pClass: Class<P>): P? {
    return if (this::class.java == pClass) {
        pClass.cast(this)
    } else {
        null
    }
}

/**
 * Classes that implement this interface should hold context of PSI tree while traversing to provide some
 * information from visited PSI elements to their subtrees.
 * For example, while build.gradle.kts dependencies analysis we should save the fact that we inside dependencies {} or
 * allProjects{} block to now the scope of declared dependencies.
 */
interface AnalyzerContext

/**
 * Classes that implement this interface represent analyzer that analyzes PSI elements using context.
 * For example, while build.gradle.kts dependencies analysis we check the fact that we inside dependencies block using
 * context [AnalyzerContext], and if it is true, we process given [PsiElement] otherwise skip.

 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param R the type of the analysis result.
 */
interface PsiAnalyzerWithContext<C : AnalyzerContext, R> {

    fun analyzeWithContext(psiElement: PsiElement, context: C): R?
}

/**
 * Classes that implement this interface represent controller which modifies analysers' context while psi tree traverse.
 * For example, while build.gradle.kts dependencies analysis we need the fact that we are inside dependencies {} block,
 * so when we visit this block, we save this fact to context using [openPsiContext], to provide this fact to element in
 * subtree, after visiting all subtree we need to remove this fact from context using [closePsiContext].
 *
 * @param C the type of [AnalyzerContext] to get context in which element analysis is precessing.
 */
interface PsiContextController<C : AnalyzerContext> {

    fun openPsiContext(psiElement: PsiElement, context: C) {}

    fun closePsiContext(psiElement: PsiElement, context: C) {}
}

/**
 * [PsiContextController] implementation which specify psi element type [P] for context controller. This implementation
 * check psi element type in [openPsiContext] and [closePsiContext] and process context change only if psi element
 * type matches [pClass] type.
 *
 * @param C the type of [AnalyzerContext] to get context in which element analysis is precessing.
 * @param P the type of PSI elements that can be analyzed.
 */
abstract class PsiContextControllerImpl<C : AnalyzerContext, P : PsiElement>(private val pClass: Class<P>) :
    PsiContextController<C> {

    open fun openContext(psiElement: P, context: C) {}

    open fun closeContext(psiElement: P, context: C) {}

    override fun openPsiContext(psiElement: PsiElement, context: C) {
        psiElement.castToClass(pClass)?.let { openContext(it, context) }
    }

    override fun closePsiContext(psiElement: PsiElement, context: C) {
        psiElement.castToClass(pClass)?.let { closeContext(it, context) }
    }
}

/**
 * [PsiAnalyzerWithContextImpl] implementation which specify psi element type [P] in analyzer. This implementation check
 * psi element type in [analyzeWithContext] and process analysis only if psi element type matches [pClass] type.
 *
 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @property pClass class to cast elements to [P] type or do not process analysis.
 */
abstract class PsiAnalyzerWithContextImpl<C : AnalyzerContext, P : PsiElement, R>(private val pClass: Class<P>) :
    PsiAnalyzerWithContext<C, R> {

    open fun analyze(psiElement: P, context: C): R? = null

    override fun analyzeWithContext(psiElement: PsiElement, context: C): R? {
        return psiElement.castToClass(pClass)?.let { analyze(it, context) }
    }
}

/**
 * Classes that extend this abstract class are used to compound the results of different analyzers.
 *
 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param R the type of the analysis result.
 * @param T the type of aggregator result.
 */
abstract class AnalyzersAggregatorWithContext<C : AnalyzerContext, R, T> {

    abstract fun aggregate(analyzerToStat: AnalyzerWithContextToStat<C, R>): T
}

/**
 * The main analyzer accepts a list of analyzers and an aggregator to compound the analysis results.
 * It implements analysis using a single pass of the recursive PSI visitor.
 *
 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param R the type of the analysis result.
 * @param T the type of the aggregator result.
 * @param analyzers list containing secondary analyzers to process [PsiElement] using mutable context.
 * @property aggregator used to compound results of secondary analyzers.
 */
open class PsiMainAnalyzerWithContext<C : AnalyzerContext, R, T>(
    private val controllers: List<PsiContextController<C>>,
    private val analyzers: List<PsiAnalyzerWithContext<C, R>>,
    private val aggregator: AnalyzersAggregatorWithContext<C, R, T>
) {

    fun analyze(psiElement: PsiElement, context: C): T {
        val visitor = VisitorWithContext(controllers, analyzers, context)
        psiElement.accept(visitor)
        return aggregator.aggregate(visitor.analyzerToStat)
    }
}

/**
 * Recursive visitor that accepts the list of secondary analyzers, performs analysis of PSI element
 * using all [analyzers]. Saves the results to [analyzerToStat].
 *
 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param R the type of the analysis result.
 * @property analyzerToStat mapping from the analyzer to its computed results.
 */
class VisitorWithContext<C : AnalyzerContext, R>(
    private val controllers: List<PsiContextController<C>>,
    private val analyzers: List<PsiAnalyzerWithContext<C, R>>,
    private val context: C
) : PsiRecursiveElementVisitor() {

    val analyzerToStat: AnalyzerWithContextToStat<C, R> = analyzers.associateBy({ it }, { mutableMapOf() })

    override fun visitElement(element: PsiElement) {
        controllers.forEach { controller -> controller.openPsiContext(element, context) }

        analyzers.forEach { analyzer ->
            analyzer.analyzeWithContext(element, context)?.let {
                analyzerToStat[analyzer]?.set(element, it)
            }
        }
        super.visitElement(element)

        controllers.forEach { controller -> controller.closePsiContext(element, context) }
    }
}
