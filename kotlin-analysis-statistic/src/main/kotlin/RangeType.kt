package org.jetbrains.research.ml.kotlinAnalysis

enum class RangeType(val fqName: String?, val simpleName: String?) {
    UNTIL("kotlin.ranges.until", "until"),
    DOWN_TO("kotlin.ranges.downTo", "downTo"),
    DOTS("kotlin.*.rangeTo", "rangeTo"),
    RANGE_TO("kotlin.*.rangeTo", "rangeTo"),
    NOT_RANGE(null, null);

    companion object {
        private val mapFqNames = values().associateBy(RangeType::fqName)
        fun fromFqName(type: String) = mapFqNames[type]

        private val mapSimpleNames = values().associateBy(RangeType::simpleName)
        fun fromName(type: String) = mapSimpleNames[type]

        fun valuesNotNull(): List<RangeType> {
            return values().filter { it != NOT_RANGE }
        }
    }
}
