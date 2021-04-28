package org.jetbrains.research.ml.kotlinAnalysis.psi.data

import java.util.*

fun main(args: Array<String>) {
    val list = ArrayList<Any>()
    list.add("Hello")
    list.add(0)
    list.map(::println)
}
