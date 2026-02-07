package com.saionji.mysensor.domain

import androidx.compose.ui.graphics.Color
import com.saionji.mysensor.data.MapSensor
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
    ): List<MapSensor> {
        val sensors = repository.getSensorDataByArea(lat1, lon1, lat2, lon2)

        sensors.forEach { sensor ->
            sensor.measurements.forEach {
                when (it.valueType) {
                    "P0" -> {
                        it.valueType = "PM1"
                    }
                    "P1" -> {
                        it.valueType = "PM10"
                    }
                    "P2" -> {
                        it.valueType = "PM2.5"
                    }
                    "temperature" -> {
                    }
                    "humidity" -> {
                    }
                    "noise_LAeq" -> {
                        it.valueType = "noise LAeq"
                    }
                    "pressure" -> {
                        it.value = (it.value.div(100)).roundToInt().toDouble()
                    }
                }
            }
        }

        return sensors
    }
}