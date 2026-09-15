package com.saionji.mysensor.shared.domain.usecase

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.ColorResolver
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import kotlin.math.roundToInt

class GetSensorValuesUseCase(
    private val mySensorRepository: MySensorRepository
) {

    private val sensorOrder = mapOf(
        "PM1" to 0,
        "PM2.5" to 1,
        "PM10" to 2,
        "temperature" to 3,
        "humidity" to 4,
        "pressure" to 5,
        "noise LAeq" to 6
    )

    suspend operator fun invoke(device: SettingsSensor): List<MySensor> {

        return mySensorRepository
            .getSensor(device.id)
            .filter { it.valueType != "pressure_at_sealevel" }
            .mapNotNull { sensor ->
                when (sensor.valueType) {
                    "P0" -> sensor.copy(
                        valueType = "PM1",
                        value = "${sensor.value}µg/m³"
                    )

                    "P1" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        sensor.copy(
                            valueType = "PM10",
                            color = ColorResolver.resolveColorInt("PM10", numericValue),
                            value = "${sensor.value}µg/m³"
                        )
                    }

                    "P2" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        sensor.copy(
                            valueType = "PM2.5",
                            color = ColorResolver.resolveColorInt("PM2.5", numericValue),
                            value = "${sensor.value}µg/m³"
                        )
                    }

                    "temperature" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        sensor.copy(
                            color = ColorResolver.resolveColorInt("temperature", numericValue),
                            value = "${numericValue.roundToInt()}°C"
                        )
                    }

                    "humidity" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        sensor.copy(
                            color = ColorResolver.resolveColorInt("humidity", numericValue),
                            value = "${numericValue.roundToInt()}% RH"
                        )
                    }

                    "noise_LAeq" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        sensor.copy(
                            valueType = "noise LAeq",
                            color = ColorResolver.resolveColorInt("noise LAeq", numericValue),
                            value = "${sensor.value}dBA"
                        )
                    }

                    "pressure" -> {
                        val numericValue = sensor.value.toDoubleOrNull()
                            ?: return@mapNotNull null

                        val pressure = numericValue / 100

                        sensor.copy(
                            color = ColorResolver.resolveColorInt("pressure", pressure),
                            value = "${pressure.roundToInt()}hPA"
                        )
                    }

                    else -> sensor
                }
            }
            .sortedBy { sensorOrder[it.valueType] ?: Int.MAX_VALUE }
    }
}