package org.jetbrains.research.ml.kotlinAnalysis.psi.data

fun main() {
    val c = 5  // some comment
    outer@ while (true) {
        var count1 = 0 // some comment
        var count2 = 0 // some comment
        /*
         * some kdoc
         * some kdoc
         */
        do {
            TODO()
        } while (true)
    }
}
