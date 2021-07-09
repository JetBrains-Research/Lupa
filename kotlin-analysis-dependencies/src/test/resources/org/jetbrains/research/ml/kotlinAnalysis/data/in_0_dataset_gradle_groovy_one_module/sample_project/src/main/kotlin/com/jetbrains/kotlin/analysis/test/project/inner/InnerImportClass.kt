package com.jetbrains.kotlin.analysis.test.project.inner

import com.jetbrains.kotlin.analysis.test.project.multi.MultiImportClass
import kotlinx.coroutines.delay

class InnerImportClass {

    suspend fun importUsage(): Int {
        val c = MultiImportClass()
        delay(c.importUsage().toLong())
        return 29
    }
}
