package com.saionji.mysensor.shared.ui.viewmodel

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class ThrottleExecutor(
    private val delayMillis: Long
) {
    private var lastExecutedMark: TimeSource.Monotonic.ValueTimeMark? = null

    suspend fun run(block: suspend () -> Unit) {
        val now = TimeSource.Monotonic.markNow()
        val lastMark = lastExecutedMark

        if (lastMark == null || lastMark.elapsedNow() >= delayMillis.milliseconds) {
            lastExecutedMark = now
            block()
        }
    }
}