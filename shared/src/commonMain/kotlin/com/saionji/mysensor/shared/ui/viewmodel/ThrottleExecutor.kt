package com.saionji.mysensor.shared.ui.viewmodel

import kotlinx.datetime.Clock

class ThrottleExecutor(
    private val delayMillis: Long
) {
    private var lastExecuted = 0L

    suspend fun run(block: suspend () -> Unit) {
        val now = Clock.System.now().toEpochMilliseconds()

        if (now - lastExecuted >= delayMillis) {
            lastExecuted = now
            block()
        }
    }
}