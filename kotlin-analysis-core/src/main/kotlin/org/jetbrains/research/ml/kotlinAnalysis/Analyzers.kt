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
 * [PsiAnalyzerWithContextImpl] implementation which specify psi element type [P] in analyzer. This implementation check
 * psi element type in [analyzeWithContext] and process analysis only if psi element type matches [pClass] type.
 *
 * @param C the type of [AnalyzerContext] to get context in which element analysis is precessing.
 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @property pClass class to cast elements to [P] type or do not process analysis.
 */
abstract class PsiAnalyzerWithContextImpl<P : PsiElement, C : AnalyzerContext, R>(private val pClass: Class<P>) :
    PsiAnalyzerWithContext<C, R> {

    abstract fun analyze(psiElement: P, context: C): R?

    override fun analyzeWithContext(psiElement: PsiElement, context: C): R? {
        return psiElement.castToClass(pClass)?.let { analyze(it, context) }
    }
}

/** Null object for [AnalyzerContext] parameter, which does not store anythig. */
object AnalyzerEmptyContext : AnalyzerContext

/** [PsiAnalyzerWithContextImpl] implementation which ignores context and use [AnalyzerEmptyContext] instead. */
abstract class PsiAnalyzer<P : PsiElement, R>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, AnalyzerEmptyContext, R>(pClass) {

    abstract fun analyze(psiElement: P): R?

    override fun analyze(psiElement: P, context: AnalyzerEmptyContext): R? {
        return analyze(psiElement)
    }
}

/**
 * Classes that inherit from this interface implement analyzer that analyzes the concrete type of PSI elements with a cache.
 * The cache is used to memorize the results of the analysis.
 *
 * For example, for the task "check if a class is immutable, which means it has only val fields,
 * and the type of all fields is also immutable" we can cache the result of this analysis.
 * It allows to avoid analyzing the same objects twice if the class has several fields with the same type.
 *
 * @param C the type of [AnalyzerContext] to get context in  which element analysis is precessing.
 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @param T the type of the cache key.
 */
abstract class PsiAnalyzerWithCache<P : PsiElement, C : AnalyzerContext, R, T : Any>(pClass: Class<P>) :
    PsiAnalyzerWithContextImpl<P, C, R>(pClass) {
    abstract val cache: MutableMap<T, R>

    /**
     * Method to compute the key by PSI element. This key would be used to memorize of the analysis result
     * for the corresponding PSI element.
     */
    abstract fun PsiElement.cacheKey(): T

    /**
     * Analyzes a [PSI element][psiElement] and returns the analysis result,
     * for example, fully qualified name for a class or a function.
     *
     * If the analysis result is already computed for the corresponding key,
     * the method returns the value stored in the cache.
     * Otherwise, it runs [analyzeIfNotCached].
     */
    override fun analyze(psiElement: P, context: C): R? {
        val psiElementKey = psiElement.cacheKey()
        return cache[psiElementKey] ?: analyzeIfNotCached(psiElement)?.let {
            cache[psiElementKey] = it
            it
        }
    }

    /**
     * Analyzes a [PSI element][psiElement] and returns the analysis result, for example,
     * fully qualified name for a class or a function.
     * The method is used when there is no stored value for the [psiElement.cacheKey()] in the cache.
     */
    abstract fun analyzeIfNotCached(psiElement: P): R?
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
