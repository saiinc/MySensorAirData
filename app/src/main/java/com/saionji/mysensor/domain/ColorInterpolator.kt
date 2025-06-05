package com.saionji.mysensor.domain

import kotlin.math.roundToInt

// Универсальная функция для интерполяции цвета на основе значения и списка цветовых диапазонов
fun interpolateColor(value: Double, ranges: List<Triple<Double, Double, Triple<Int, Int, Int>>>): Int {
    fun linearInterpolate(value: Double, start: Double, end: Double, colorStart: Int, colorEnd: Int): Int {
        return ((colorStart + (value - start) / (end - start) * (colorEnd - colorStart)).roundToInt()).coerceIn(0, 255)
    }

    if (value < ranges.first().first) {
        val (r, g, b) = ranges.first().third
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    if (value > ranges.last().second) {
        val (r, g, b) = ranges.last().third
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    for ((i, range) in ranges.withIndex()) {
        val (start, end, colorStart) = range
        if (value in start..end) {
            val colorEnd = ranges.getOrNull(i + 1)?.third ?: colorStart

            val r = linearInterpolate(value, start, end, colorStart.first, colorEnd.first)
            val g = linearInterpolate(value, start, end, colorStart.second, colorEnd.second)
            val b = linearInterpolate(value, start, end, colorStart.third, colorEnd.third)

            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    return 0xFF000000.toInt()
}