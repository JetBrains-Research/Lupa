package org.jetbrains.research.ml.kotlinAnalysis.data

fun a(value: Int, charValue: String) {
    require(value in 1 until 10)

    require(charValue in "b".rangeTo("e"))
}
