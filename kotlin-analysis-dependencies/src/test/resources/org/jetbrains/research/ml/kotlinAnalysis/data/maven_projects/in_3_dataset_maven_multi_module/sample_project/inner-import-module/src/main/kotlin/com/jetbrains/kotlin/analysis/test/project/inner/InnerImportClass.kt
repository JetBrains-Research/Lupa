package com.jetbrains.kotlin.analysis.test.project.inner

import com.jetbrains.kotlin.analysis.test.project.multi.MultiImportClass
import kotlinx.coroutines.delay

class InnerImportClass {

    suspend fun importUsage(): Int {
        delay(MultiImportClass().importUsage().toLong())
        return 29
    }

}
