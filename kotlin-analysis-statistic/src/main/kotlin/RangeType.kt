package org.jetbrains.research.ml.kotlinAnalysis

enum class RangeType(val value: String) {
    UNTIL("until"),
    DOWN_TO("downTo"),
    RANGE_TO("rangeTo"),
    DOTS("dots"),
    NOT_RANGE("notRange");

    companion object {
        private val map = values().associateBy(RangeType::value)
        fun fromString(type: String) = map[type]
    }
}
