package org.jetbrains.research.ml.kotlinAnalysis.data

class in_7_map {

    val r = (2 until 10).map { doSomething(it) }.toList()

    private fun doSomething(it: Int): Int {
        return it
    }

    fun a(): List<Long> {
       return  (1L .. 2L).map { it + 1 }
    }


}
