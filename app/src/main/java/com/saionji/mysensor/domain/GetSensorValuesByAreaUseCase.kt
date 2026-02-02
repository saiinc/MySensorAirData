package com.saionji.mysensor.domain

import androidx.compose.ui.graphics.Color
import com.saionji.mysensor.data.MySensorRepository
import com.saionji.mysensor.network.model.MySensorRawData
import kotlin.math.roundToInt

class GetSensorValuesByAreaUseCase(private val repository: MySensorRepository
) {
    suspend operator fun invoke(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData> {
        val sensors = repository.getSensorDataByArea(lat1, lon1, lat2, lon2)

        sensors.forEach { sensor ->
            sensor.sensordatavalues.forEach {
                when (it.valueType) {
                    "P0" -> {
                        it.valueType = "PM1"
                    }
                    "P1" -> {
                        it.valueType = "PM10"
                        it.color = Color(interpolateColor(it.value, PM10_COLOR_RANGES))
                    }
                    "P2" -> {
                        it.valueType = "PM2.5"
                        it.color = Color(interpolateColor(it.value, PM25_COLOR_RANGES))
                    }
                    "temperature" -> {
                        it.color = Color(interpolateColor(it.value, TEMPERATURE_COLOR_RANGES))
                    }
                    "humidity" -> {
                        it.color = Color(interpolateColor(it.value, HUMIDITY_COLOR_RANGES))
                    }
                    "noise_LAeq" -> {
                        it.valueType = "noise LAeq"
                        it.color = Color(interpolateColor(it.value, NOISE_COLOR_RANGES))
                    }
                    "pressure" -> {
                        it.value = (it.value / 100).roundToInt().toDouble()
                        it.color = Color(interpolateColor(it.value / 100, PRESSURE_COLOR_RANGES))
                    } // hPa
                    else -> it.color = Color.Transparent
                }
            }
        }

        return sensors
    }
}