package com.jetbrains.kotlin.analysis.test.project

import com.jetbrains.kotlin.analysis.test.project.MultiImportClass
import kotlinx.coroutines.delay

class InnerImportClass {

    suspend fun importUsage(): Int {
        val c = MultiImportClass()
        delay(c.importUsage().toLong())
        return 29
    }
}

