package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor

typealias AnalyzerToStat<P, R> = Map<PsiAnalyzer<P, R>, MutableMap<P, R>>

/**
 * Classes that implement this interface represent analyzer that analyzes the concrete type of PSI elements.
 * For example, for the task "get fully qualified names for classes" [P] will be [PsiClass] and [R] will be [String]
 * or any type that represents a fully qualified name.

 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 */
interface PsiAnalyzer<P : PsiElement, R> {
    /**
     * Analyzes a [PSI element][psiElement] and returns the analysis result, for example,
     * fully qualified name for a class or function.
     */
    fun analyze(psiElement: P): R
}

/**
 * Classes that inherit from this interface implement analyzer that analyzes the concrete type of PSI elements with a cache.
 * The cache is used to memorize the results of the analysis.
 *
 * For example, for the task "check if a class is immutable, which means it has only val fields,
 * and the type of all fields is also immutable" we can cache the result of this analysis.
 * It allows to avoid analyzing the same objects twice if the class has several fields with the same type.

 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @param T the type of the cache key.
 */
abstract class PsiAnalyzerWithCache<P : PsiElement, R, T : Any> : PsiAnalyzer<P, R> {
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
    override fun analyze(psiElement: P): R {
        return cache.getOrPut(psiElement.cacheKey()) { analyzeIfNotCached(psiElement) }
    }

    /**
     * Analyzes a [PSI element][psiElement] and returns the analysis result, for example,
     * fully qualified name for a class or a function.
     * The method is used when there is no stored value for the [psiElement.cacheKey()] in the cache.
     */
    abstract fun analyzeIfNotCached(psiElement: P): R
}

/**
 * Classes that extend this abstract class are used to compound the results of different analyzers.
 *
 * @param P the type of PSI elements that were analyzed by analyzers.
 * @param R the type of the analysis result.
 * @param T the type of aggregator result.
 */
abstract class AnalyzersAggregator<P : PsiElement, R, T> {
    /**
     * Aggregates the results of different analyzers.
     * @param analyzerToStat mapping from the analyzer to its analysis results.
     */
    abstract fun aggregate(analyzerToStat: AnalyzerToStat<P, R>): T
}

/**
 * The main analyzer accepts a list of analyzers and an aggregator to compound the analysis results.
 * It implements analysis using a single pass of the recursive PSI visitor.
 *
 * For example, for a class, we can analyze all functions in this class and for each function aggregate
 * the results of this analysis, e.g. count arguments or types of arguments and so on.
 *
 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @param T the type of the aggregator result.
 * @param analyzers list containing secondary analyzers of PSI elements with type [P].
 * @property aggregator used to compound results of secondary analyzers.
 * @property visitor used to perform recursive traversal of PSI and analysis of each node with type [P].
 */
open class PsiMainAnalyzer<P : PsiElement, R, T>(
    analyzers: List<PsiAnalyzer<P, R>>,
    private val aggregator: AnalyzersAggregator<P, R, T>
) {
    private val visitor: Visitor<P, R> = Visitor(analyzers)

    /**
     * Performs recursive traversal of the [PSI element][psiElement],
     * analyzes each node with type [P] using all analyzers
     * and aggregates the results using [aggregator] for each node with type [P].
     * @return result of the aggregation.
     */
    fun analyze(psiElement: PsiElement): T {
        psiElement.accept(visitor)
        return aggregator.aggregate(visitor.analyzerToStat)
    }
}

/**
 * Recursive visitor that accepts the list of secondary analyzers, performs analysis of PSI element
 * using all [analyzers]. Saves the results to [analyzerToStat].
 *
 * @param P the type of PSI elements that can be analyzed.
 * @param R the type of the analysis result.
 * @property analyzerToStat mapping from the analyzer to its computed results.
 */
class Visitor<P : PsiElement, R>(private val analyzers: List<PsiAnalyzer<P, R>>) :
    PsiRecursiveElementVisitor() {
    val analyzerToStat: AnalyzerToStat<P, R> = analyzers.associateBy({ it }, { mutableMapOf() })

    override fun visitElement(element: PsiElement) {
        analyzers.forEach { analyzer ->
            (element as? P)?.let {
                analyzerToStat[analyzer]?.set(element, analyzer.analyze(element))
            }
        }
        super.visitElement(element)
    }
}
