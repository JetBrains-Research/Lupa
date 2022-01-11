package org.jetbrains.research.ml.kotlinAnalysis.data

class in_6_foreach {

    fun a(c: Int, d: Int) {
        (1..10).forEach {
            println(it)
        }

        c.rangeTo(d).forEach {
            (d downTo c).forEach {

            }
        }
    }
}
