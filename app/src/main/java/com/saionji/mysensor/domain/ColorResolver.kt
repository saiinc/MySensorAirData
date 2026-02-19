package com.saionji.mysensor.domain

object ColorResolver {
    fun resolveColorInt(
        valueType: String,
        value: Double
    ): Int {
        val ranges = when (valueType) {
            "PM2.5" -> PM25_COLOR_RANGES
            "PM10" -> PM10_COLOR_RANGES
            "temperature" -> TEMPERATURE_COLOR_RANGES
            "humidity" -> HUMIDITY_COLOR_RANGES
            "pressure" -> PRESSURE_COLOR_RANGES
            "noise LAeq" -> NOISE_COLOR_RANGES
            else -> PM25_COLOR_RANGES
        }

        return interpolateColor(
            value = value,
            ranges = ranges
        )
    }
}