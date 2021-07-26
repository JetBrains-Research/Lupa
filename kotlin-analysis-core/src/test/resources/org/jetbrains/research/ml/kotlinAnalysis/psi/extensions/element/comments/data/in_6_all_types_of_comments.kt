package org.jetbrains.research.ml.kotlinAnalysis.psi.extensions.data

/**
 * class kdoc
 */
class SimpleClass {
    /**
     * kdoc
     *
     * @author author
     */
    fun method1() {
        val a = 5
        /*
        multiline comment
        another line
         */
    }

    fun method2() {
        // comment inside method
        val b = 6
    }
}
