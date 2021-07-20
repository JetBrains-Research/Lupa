package com.jetbrains.kotlin.analysis.test.multi

import kotlinx.coroutines.delay
import org.apache.commons.math3.random.JDKRandomGenerator

class MultiImportClass {

    private val generator = JDKRandomGenerator();

    suspend fun importUsage(): Int {
        delay(generator.nextLong())
        return 29
    }
}
