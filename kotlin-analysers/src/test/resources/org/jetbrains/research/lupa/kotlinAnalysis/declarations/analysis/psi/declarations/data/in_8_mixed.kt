package org.test.main.declarations

internal open class InternalClass {
    internal fun internalFun() {}
    fun publicFun() {}

    internal interface Nested
}

internal fun internalFun(): InternalClass = InternalClass()

internal expect fun internalExpectFun()


object SameFileUsages {

    internal fun test2(instance: InternalClass) {
        internalExpectFun()
        instance.internalFun()
    }

    internal fun test3() {
        internalFun().publicFun()
    }

    val topLevel: Any = InternalClass()

    private class TestClass : InternalClass()

    class TestNestedClass : InternalClass.Nested

}
