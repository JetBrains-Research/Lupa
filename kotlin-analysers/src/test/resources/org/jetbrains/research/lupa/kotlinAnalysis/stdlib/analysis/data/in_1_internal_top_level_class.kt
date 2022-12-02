package org.jetbrains.research.lupa.kotlinAnalysis.stdlib.analysis.data

class Base

interface MatchResultInterface : MatchResult

interface A : MatchResultInterface

//internal open class MatchResultClass(
//    override val groupValues: List<String>,
//    override val groups: MatchGroupCollection,
//    override val range: IntRange,
//    override val value: String
//) : MatchResult {
//    override fun next(): MatchResult? {
//        TODO("Not yet implemented")
//    }
//}
