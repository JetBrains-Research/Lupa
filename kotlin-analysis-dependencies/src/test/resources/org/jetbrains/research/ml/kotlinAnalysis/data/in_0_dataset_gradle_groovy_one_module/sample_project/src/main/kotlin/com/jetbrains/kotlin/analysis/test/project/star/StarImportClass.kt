package com.jetbrains.kotlin.analysis.test.project.star

import kotlinx.coroutines.*

class StarImportClass {

    suspend fun importUsage(): Int {
        delay(1000L)
        return 29
    }
}
