package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor

typealias AnalyzerToStat<P, R> = Map<PsiSecondaryAnalyzer<P, R>, MutableMap<P, R>>

/**
 * Classes that inherit from this interface implement secondary analyzers that analyze the concrete type of PSI elements.
 *
 * @param P the type of PSI elements, that can be analyzed.
 * @param R the type of the analysis result.
 */
interface PsiSecondaryAnalyzer<P : PsiElement, R> {
    /**
     * Returns the analysis result of a [PSI element][psiElement].
     */
    fun analyze(psiElement: P): R
}

/**
 * Classes that inherit from this interface implement secondary analyzers with a cache.
 * These analyzers analyze the concrete type of PSI elements. The cache is used to memorize the results of the analysis.
 *
 * @param P the type of PSI elements, that can be analyzed.
 * @param R the type of the analysis result.
 * @param T the type of the cache key.
 */
abstract class PsiSecondaryAnalyzerWithCache<P : PsiElement, R, T : Any> : PsiSecondaryAnalyzer<P, R> {
    abstract val cache: MutableMap<T, R>

    /**
     * Method to compute key by PSI element. This key would be used for memorization of the analysis result
     * for the corresponding PSI element.
     */
    abstract fun PsiElement.cacheKey(): T

    /**
     * Returns the analysis result of a [PSI element][psiElement].
     * If the analysis result is already computed for the corresponding key,
     * the method returns the value stored in the cache.
     * Otherwise, it runs [analyzeIfNotCached].
     */
    override fun analyze(psiElement: P): R {
        return cache.getOrPut(psiElement.cacheKey()) { analyzeIfNotCached(psiElement) }
    }

    /**
     * Returns the analysis result of a [PSI element][psiElement].
     * Method is used when there is no stored value for the [PSI element][psiElement] in the cache.
     */
    abstract fun analyzeIfNotCached(psiElement: P): R
}

/**
 * Classes that extend this abstract class are used to compound the results of different secondary analyzers.
 *
 * @param P the type of PSI elements, that were analyzed by analyzers.
 * @param R the type of the analysis result.
 * @param T the type of aggregator result.
 */
abstract class AnalyzersAggregator<P : PsiElement, R, T> {
    /**
     * Returns the result of aggregation analyzers results.
     * @param analyzerToStat mapping from the analyzer to its analysis results.
     */
    abstract fun aggregate(analyzerToStat: AnalyzerToStat<P, R>): T
}

/**
 * The main analyzer accepts a list of secondary analyzers and an aggregator to compound the analysis results.
 * It implements analysis using one-pass of the recursive PSI visitor.
 *
 * @param P the type of PSI elements, that can be analyzed.
 * @param R the type of the analysis result.
 * @param T the type of the aggregator result.
 * @param analyzers list containing secondary analyzers of PSI elements with type [P].
 * @property aggregator used to compound results of secondary analyzers.
 * @property visitor used to perform recursive traversal of PSI and analysis of each node with type [P].
 */
open class PsiMainAnalyzer<P : PsiElement, R, T>(
    analyzers: List<PsiSecondaryAnalyzer<P, R>>,
    private val aggregator: AnalyzersAggregator<P, R, T>
) {
    private val visitor: Visitor<P, R> = Visitor(analyzers)

    /**
     * Performs recursive traversal of [PSI element][psiElement],
     * analyzes each node with type [P] using all analyzers
     * and aggregates the results using [aggregator].
     * @return result of the aggregation.
     */
    fun analyze(psiElement: PsiElement): T {
        psiElement.accept(visitor)
        return aggregator.aggregate(visitor.analyzerToStat)
    }
}

/**
 * Recursive visitor, that accepts the list of secondary analyzers, performs analysis of PSI element
 * using all [analyzers]. Saves the results to [analyzerToStat].
 *
 * @param P the type of PSI elements, that can be analyzed.
 * @param R the type of the analysis result.
 * @property analyzerToStat mapping from the analyzer to its computed results.
 */
class Visitor<P : PsiElement, R>(private val analyzers: List<PsiSecondaryAnalyzer<P, R>>) :
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
