package com.saionji.mysensor.crash

import android.content.Context
import android.os.Build
import com.saionji.mysensor.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

object CrashReporter {
    private const val REPORT_FILE_NAME = "crash_report.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveReport(appContext, thread, throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    fun getPendingReport(context: Context): String? {
        val file = context.applicationContext.filesDir.resolve(REPORT_FILE_NAME)
        if (!file.exists()) {
            return null
        }

        return runCatching { file.readText() }.getOrNull()
    }

    fun clearPendingReport(context: Context) {
        val file = context.applicationContext.filesDir.resolve(REPORT_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun saveReport(context: Context, thread: Thread, throwable: Throwable) {
        val report = buildString {
            appendLine("App: My Sensor")
            appendLine("Package: ${BuildConfig.APPLICATION_ID}")
            appendLine("Version name: ${BuildConfig.VERSION_NAME}")
            appendLine("Version code: ${BuildConfig.VERSION_CODE}")
            appendLine("Build type: ${BuildConfig.BUILD_TYPE}")
            appendLine("Flavor: ${BuildConfig.FLAVOR}")
            appendLine("Android version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread: ${thread.name}")
            appendLine("Time: ${Instant.now()}")
            appendLine()
            appendLine("Stack trace:")
            appendLine(throwable.stackTraceToStringCompat())
        }

        runCatching {
            context.filesDir.resolve(REPORT_FILE_NAME).writeText(report)
        }
    }

    private fun Throwable.stackTraceToStringCompat(): String {
        val stringWriter = StringWriter()
        PrintWriter(stringWriter).use { printWriter ->
            printStackTrace(printWriter)
        }
        return stringWriter.toString()
    }
}