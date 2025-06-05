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
            sensor.sensordatavalues.forEach { data ->
                val value = data.value
                when (data.valueType) {
                    "P0" -> {
                        data.valueType = "PM1"
                    }
                    "P1" -> {
                        data.valueType = "PM10"
                        data.color = Color(interpolateColor(value, PM10_COLOR_RANGES))
                    }
                    "P2" -> {
                        data.valueType = "PM2.5"
                        data.color = Color(interpolateColor(value, PM25_COLOR_RANGES))
                    }
                    "temperature" -> {
                        data.color = Color(interpolateColor(value, TEMPERATURE_COLOR_RANGES))
                    }
                    "humidity" -> {
                        data.color = Color(interpolateColor(value, HUMIDITY_COLOR_RANGES))
                    }
                    "noise_LAeq" -> {
                        data.valueType = "noise LAeq"
                        data.color = Color(interpolateColor(value, NOISE_COLOR_RANGES))
                    }
                    "pressure" -> {
                        data.value = (data.value / 100).roundToInt().toDouble()
                        data.color = Color(interpolateColor(value / 100, PRESSURE_COLOR_RANGES))
                    } // hPa
                    else -> data.color = Color.Transparent
                }
            }
        }

        return sensors
    }
}