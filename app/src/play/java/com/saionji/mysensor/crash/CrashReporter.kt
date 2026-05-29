package com.saionji.mysensor.crash

import android.content.Context

object CrashReporter {
    fun install(context: Context) = Unit

    fun getPendingReport(context: Context): String? = null

    fun clearPendingReport(context: Context) = Unit
}