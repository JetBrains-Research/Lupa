package org.jetbrains.research.lupa.kotlinAnalysis.declarations.analysis.psi.usages.resolve.data

class A3
internal open class B3 {
    internal fun internalFun() {}
    fun publicFun() {}
}

internal expect fun internalExpectFun()

internal fun internalFun(): B3 = B3()

internal fun test2(instance: B3) {
    internalExpectFun()
    instance.internalFun()
}

internal fun test3() {
    internalFun().publicFun()
}
