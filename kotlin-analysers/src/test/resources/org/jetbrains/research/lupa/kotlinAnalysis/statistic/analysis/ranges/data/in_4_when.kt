package org.jetbrains.research.ml.kotlinAnalysis.data

class in_4_when {

    fun test(i: Int, j: Int) {
        var b = when (i) {
            in 3..5 -> 1
            in 8..9 -> 2
            else -> 9
        }


        var c = when (j) {
            in 5.rangeTo( 10) -> 3
            in 15 until 20 -> 4
            else -> 9
        }
    }
}
