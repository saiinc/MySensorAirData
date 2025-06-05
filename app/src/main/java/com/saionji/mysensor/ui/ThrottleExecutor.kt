package com.saionji.mysensor.ui

class ThrottleExecutor(
    private val delayMillis: Long
) {
    private var lastExecuted = 0L

    suspend fun run(block: suspend () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastExecuted >= delayMillis) {
            lastExecuted = now
            block()
        }
    }
}