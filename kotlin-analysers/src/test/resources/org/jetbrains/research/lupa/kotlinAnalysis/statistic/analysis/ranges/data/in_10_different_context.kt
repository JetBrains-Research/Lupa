package org.jetbrains.research.ml.kotlinAnalysis.data

class in_10_diffrent_context {

    private fun String.isIdentifier() = !isEmpty() && first().isIdentifierStart() && all { it.isIdentifierStart() || it in '0'..'9' }

    fun main(j: Int) {
        val a = (1..5)
        val b = "a".."c"

        val c = 1.rangeTo(10)
        val d = "a".rangeTo("c")

        val e = 3456789098726 until 34134

        for (i in (1 until 10)) {

        }

        for (i in 1 downTo -1) {

        }

        for (i in 1.rangeTo(10)) {

        }

        if (j in 1L.rangeTo(1000L)) {

        }

        (1 until 19).forEach {

        }

        1.rangeTo(10).forEach {

        }


        when(j) {
            in 0.rangeTo(50) -> 3
            in 50.rangeTo(100) ->  5
        }

        when (a) {
            in (0..1) -> null
            in 241 until 2414 -> null
        }

        var i = 6
        while (i in 1..10) {
            i++
        }

        require(j in 1..6) { "value must be in 1 and 6 inclusive" }
    }
}

private fun Char.isIdentifierStart(): Boolean {
    return false
}
