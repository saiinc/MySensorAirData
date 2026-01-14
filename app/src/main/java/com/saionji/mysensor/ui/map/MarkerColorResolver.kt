package com.saionji.mysensor.ui.map

import com.saionji.mysensor.domain.HUMIDITY_COLOR_RANGES
import com.saionji.mysensor.domain.NOISE_COLOR_RANGES
import com.saionji.mysensor.domain.PM10_COLOR_RANGES
import com.saionji.mysensor.domain.PM25_COLOR_RANGES
import com.saionji.mysensor.domain.PRESSURE_COLOR_RANGES
import com.saionji.mysensor.domain.TEMPERATURE_COLOR_RANGES
import com.saionji.mysensor.domain.interpolateColor

object MarkerColorResolver {

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