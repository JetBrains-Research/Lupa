package org.jetbrains.research.ml.kotlinAnalysis.reflection

enum class JavaReflectionFunction(val simpleName: String?) {
    FOR_NAME("forName"),
    DECLARED_METHODS("declaredMethods"),
    GET_DECLARED_METHOD("getDeclaredMethod"),
    ANNOTATED_INTERFACES("annotatedInterfaces"),
    ANNOTATED_SUPERCLASS("annotatedSuperclass"),
    CONSTRUCTORS("constructors"),
    DECLARED_CONSTRUCTORS("declaredConstructors"),
    GET_DECLARED_CONSTRUCTOR("getDeclaredConstructor"),
    NOT_REFLECTION(null);

    companion object {
        private val mapSimpleNames = values().associateBy(JavaReflectionFunction::simpleName)
        fun fromName(type: String) = mapSimpleNames[type]
    }
}
