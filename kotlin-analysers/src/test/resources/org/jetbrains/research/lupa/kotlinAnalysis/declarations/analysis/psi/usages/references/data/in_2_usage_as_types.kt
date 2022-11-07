package org.jetbrains.research.lupa.kotlinAnalysis.psi.data

class A2
internal open class B2 {
    internal interface Nested
}

val topLevel: Any = B2()

private class TestClass : B2()

class TestNestedClass : B2.Nested
